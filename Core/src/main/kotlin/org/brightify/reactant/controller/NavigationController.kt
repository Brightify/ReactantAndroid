package org.brightify.reactant.controller

import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.ReplaySubject
import org.brightify.reactant.R
import org.brightify.reactant.core.ControllerWithResult
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.core.ReactantActivity
import java.util.Stack

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class NavigationController(initialController: ViewController?) : ViewController() {

    var isNavigationBarHidden = false

    val toolbar = Toolbar(ReactantActivity.globalContext)

    private val layout = AutoLayout(ReactantActivity.globalContext)
    private val layoutContent = FrameLayout(ReactantActivity.globalContext)
    private val viewControllerStack = Stack<ViewController>()
    private val lifetimeDisposeBag = CompositeDisposable()
    private val toolbarHeight = 56 // FIXME get correct value

    init {
        tabBarItem = initialController?.tabBarItem
        loadViewIfNeeded()
        initialController?.let { push(it, animated = false) }
    }

    constructor() : this(null)

    override fun loadView() {
        super.loadView()

        toolbar.navigationIcon = ReactantActivity.instance.resources.getDrawable(R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            if (viewControllerStack.size > 1) {
                pop()
            }
        }

        view = FrameLayout(ReactantActivity.globalContext)

        layout.children(toolbar, layoutContent)

        toolbar.snp.makeConstraints {
            top.left.right.equalToSuperview()
            height.equalTo(toolbarHeight)
        }

        layoutContent.snp.makeConstraints {
            top.equalTo(toolbar.snp.bottom)
            bottom.left.right.equalToSuperview()
        }
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        clearLayout(false)
        showViewController()
    }

    override fun viewDidAppear() {
        super.viewDidAppear()

        viewControllerStack.peek().viewDidAppear()
    }

    override fun viewWillDisappear() {
        super.viewWillDisappear()

        viewControllerStack.peek().viewWillDisappear()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()

        viewControllerStack.peek().viewDidDisappear()
    }

    override fun onBackPressed(): Boolean {
        if (viewControllerStack.peek().onBackPressed()) {
            return true
        }

        if (viewControllerStack.size > 1) {
            pop()
            return true
        } else {
            return false
        }
    }

    fun setNavigationBarHidden(hidden: Boolean, animated: Boolean = true) {
        isNavigationBarHidden = hidden
    }

    fun push(viewController: ViewController, animated: Boolean = true) {
        clearLayout(!viewControllerStack.empty())
        viewControllerStack.push(viewController)
        showViewController()
    }

    fun pop(animated: Boolean = true): ViewController? {
        if (viewControllerStack.empty()) {
            return null
        }

        clearLayout()
        val viewController = viewControllerStack.pop()
        showViewController()
        return viewController
    }

    fun replace(viewController: ViewController, animated: Boolean = true): ViewController? {
        return pop().also { push(viewController, animated) }
    }

    fun replaceAll(viewController: ViewController, animated: Boolean = true): List<ViewController> {
        clearLayout(!viewControllerStack.empty())
        val viewControllers = viewControllerStack.elements().toList()
        viewControllerStack.clear()
        push(viewController, animated)
        return viewControllers
    }

    fun <C : ViewController> push(viewController: Observable<C>, animated: Boolean = true) {
        viewController.subscribe { push(it, animated) }.addTo(lifetimeDisposeBag)
    }

    fun <C : ViewController> replace(viewController: Observable<C>, animated: Boolean = true): Observable<ViewController?> {
        val replacedController = ReplaySubject.create<ViewController?>(1)
        viewController
                .subscribeBy(
                        onNext = { controller ->
                            replacedController.onNext(replace(viewController = controller, animated = animated))
                        },
                        onComplete = { replacedController.onComplete() }
                )
                .addTo(lifetimeDisposeBag)
        return replacedController
    }

    fun <C : ViewController> popAllAndReplace(viewController: Observable<C>): Observable<List<ViewController>> {
        return replaceAll(viewController, false)
    }

    fun <C : ViewController> replaceAll(viewController: Observable<C>, animated: Boolean = true): Observable<List<ViewController>> {
        val oldControllers = ReplaySubject.create<List<ViewController>>(1)
        viewController
                .subscribeBy(
                        onNext = { oldControllers.onNext(replaceAll(viewController = it, animated = animated)) },
                        onComplete = { oldControllers.onComplete() }
                )
                .addTo(lifetimeDisposeBag)

        return oldControllers
    }

    fun <C : ViewController, T> push(viewController: Observable<ControllerWithResult<C, T>>, animated: Boolean = true): Observable<T> {
        val sharedController = viewController.replay(1).refCount()

        sharedController.map { it.controller }
                .subscribeBy(
                        onNext = { push(viewController = it, animated = animated) }
                )
                .addTo(lifetimeDisposeBag)

        return sharedController.flatMap { it.result }
    }

    private fun clearLayout(callCallbacks: Boolean = true) {
        if (callCallbacks) {
            viewControllerStack.peek().viewWillDisappear()
        }
        layoutContent.removeAllViews()
        (view as? ViewGroup)?.removeAllViews()
        if (callCallbacks) {
            viewControllerStack.peek().viewDidDisappear()
        }
        isNavigationBarHidden = false
    }

    private fun showViewController() {
        toolbar.menu.clear()
        viewControllerStack.peek().navigationController = this
        viewControllerStack.peek().loadViewIfNeeded()
        viewControllerStack.peek().viewWillAppear()
        if (isNavigationBarHidden) {
            (view as ViewGroup).addView(viewControllerStack.peek().view)
        } else {
            layoutContent.addView(viewControllerStack.peek().view)
            (view as ViewGroup).addView(layout)
        }
    }
}
