package org.brightify.reactant.controller

import android.support.v7.widget.Toolbar

import android.view.ViewGroup
import android.widget.FrameLayout
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.R
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.controller.util.TransactionManager
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.util.onChange
import java.util.Stack

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class NavigationController(private val initialController: ViewController?) : ViewController() {

    val lifetimeDisposeBag = CompositeDisposable()

    var isNavigationBarHidden: Boolean by onChange(false) { _, _, _ ->
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    val toolbar = Toolbar(ReactantActivity.context)
    private val layout = AutoLayout(ReactantActivity.context)
    private val layoutContent = FrameLayout(ReactantActivity.context)
    private val viewControllerStack = Stack<ViewController>()
    private val toolbarHeight = 56 // FIXME get correct value
    private val transactionManager = TransactionManager()

    init {
        loadViewIfNeeded()
    }

    constructor() : this(null)

    override fun loadView() {
        super.loadView()

        toolbar.navigationIcon = resources.getDrawable(R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { pop() }

        view = FrameLayout(ReactantActivity.context)

        layout.children(toolbar, layoutContent)

        toolbar.snp.makeConstraints {
            top.left.right.equalToSuperview()
            height.equalTo(toolbarHeight)
        }

        layoutContent.snp.makeConstraints {
            top.equalTo(toolbar.snp.bottom)
            bottom.left.right.equalToSuperview()
        }

        initialController?.navigationController = this
        initialController?.loadViewIfNeeded()
        initialController?.let { viewControllerStack.push(it) }
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        transactionManager.transaction {
            clearLayout(false)
            showViewController()
        }
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
        transactionManager.transaction {
            clearLayout(!viewControllerStack.empty())
            viewControllerStack.push(viewController)
            showViewController()
            viewController.viewDidAppear()
        }
    }

    fun pop(animated: Boolean = true): ViewController? {
        return transactionManager.transaction {
            if (viewControllerStack.size < 2) {
                return@transaction null
            }

            clearLayout(true)
            val viewController = viewControllerStack.pop()
            showViewController()
            viewControllerStack.peek().viewDidAppear()
            return@transaction viewController
        }
    }

    fun replace(viewController: ViewController, animated: Boolean = true): ViewController? {
        return transactionManager.transaction {
            clearLayout(!viewControllerStack.empty())
            val old = viewControllerStack.pop()
            viewControllerStack.push(viewController)
            showViewController()
            viewControllerStack.peek().viewDidAppear()
            return@transaction old
        }
    }

    fun replaceAll(viewController: ViewController, animated: Boolean = true): List<ViewController> {
        return transactionManager.transaction {
            clearLayout(!viewControllerStack.empty())
            val viewControllers = viewControllerStack.elements().toList()
            viewControllerStack.clear()
            viewControllerStack.push(viewController)
            showViewController()
            viewController.viewDidAppear()
            return@transaction viewControllers
        } ?: emptyList()
    }

    fun invalidateChild() {
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    private fun clearLayout(callCallbacks: Boolean) {
        if (callCallbacks) {
            viewControllerStack.peek().viewWillDisappear()
        }
        layoutContent.removeAllViews()
        (view as ViewGroup).removeAllViews()
        if (callCallbacks) {
            viewControllerStack.peek().viewDidDisappear()
        }
    }

    private fun showViewController() {
        toolbar.menu.clear()
        isNavigationBarHidden = false
        viewControllerStack.peek().navigationController = this
        viewControllerStack.peek().loadViewIfNeeded()
        viewControllerStack.peek().viewWillAppear()
        tabBarController?.setTabBarHidden(viewControllerStack.peek().hidesBottomBarWhenPushed)
        addViewToHierarchy()
    }

    private fun addViewToHierarchy() {
        if (isNavigationBarHidden) {
            (view as ViewGroup).addView(viewControllerStack.peek().view)
        } else {
            layoutContent.addView(viewControllerStack.peek().view)
            (view as ViewGroup).addView(layout)
        }
    }
}
