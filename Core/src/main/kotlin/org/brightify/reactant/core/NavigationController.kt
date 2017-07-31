package org.brightify.reactant.core

import android.app.FragmentManager
import android.app.FragmentTransaction
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.ReplaySubject
import org.brightify.reactant.R
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.util.children
import org.brightify.reactant.core.constraint.util.snp
import org.brightify.reactant.core.util.getFragmentAtIndex
import org.brightify.reactant.core.util.push
import org.brightify.reactant.core.util.top

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class NavigationController(private val initialController: ViewController?) : ViewController() {

    private val lifetimeDisposeBag = CompositeDisposable()
    private val transactionsMadeBeforeInitialization = ArrayList<() -> Unit>()
    private var initialized = false

    override val tabBarItem: TabBarItem?
        get() = initialController?.tabBarItem

    private val childFragmentManager: FragmentManager
        get() = viewControllerWrapper.childFragmentManager

    private val toolbarHeight: Int
        get() = 56 // FIXME get correct value

    lateinit var toolbar: Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var toolbarHeightConstraint: Constraint

    private val backStackChangeListener = { invalidateBarsVisibility(childFragmentManager.top?.viewController) }

    init {
        initialController?.let { push(it, animated = false) }
    }

    constructor() : this(null)

    override fun onCreate() {
        frameLayout = FrameLayout(activity)
        frameLayout.assignId()

        toolbar = Toolbar(activity)
        toolbar.navigationIcon = activity.resources.getDrawable(R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            if (childFragmentManager.backStackEntryCount > 1) {
                pop()
            }
        }

        contentView = AutoLayout(activity).children(toolbar, frameLayout)
        contentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        toolbar.snp.makeConstraints {
            top.left.right.equalToSuperview()
            toolbarHeightConstraint = height.equalTo(toolbarHeight).priority(ConstraintPriority.high)
        }
        toolbar.snp.disableIntrinsicSize()
        frameLayout.snp.makeConstraints {
            top.equalTo(toolbar.snp.bottom)
            bottom.left.right.equalToSuperview()
        }
        frameLayout.snp.disableIntrinsicSize()
    }

    override fun onResume() {
        super.onResume()

        childFragmentManager.removeOnBackStackChangedListener(backStackChangeListener)
        childFragmentManager.addOnBackStackChangedListener(backStackChangeListener)

        initialized = true
        transactionsMadeBeforeInitialization.forEach { it() }
        transactionsMadeBeforeInitialization.clear()
        toolbarHeightConstraint.offset(toolbarHeight)

        childFragmentManager.top?.viewController?.let {
            navigationController = this
            invalidateBarsVisibility(it)
            toolbar.menu.clear()
        }
    }

    override fun onPause() {
        initialized = false
    }

    override fun onBackPressed(): Boolean {
        if (childFragmentManager.top?.viewController?.onBackPressed() == true) {
            return true
        }

        val stackSize = childFragmentManager.backStackEntryCount
        if (stackSize > 1) {
            pop()
            return true
        } else {
            return false
        }
    }

    fun push(controller: ViewController, animated: Boolean = true) {
        transaction {
            controller.navigationController = this
            toolbar.menu.clear()
            val transaction = childFragmentManager.beginTransaction()
            transaction.push(frameLayout.id, controller.viewControllerWrapper)
            transaction.setTransition(if (animated) FragmentTransaction.TRANSIT_FRAGMENT_OPEN else FragmentTransaction.TRANSIT_NONE)
            transaction.commit()
        }
    }

    fun pop(animated: Boolean = true): ViewController? {
        return transaction {
            childFragmentManager.getFragmentAtIndex(
                    childFragmentManager.backStackEntryCount - 2)?.viewController?.navigationController = this
            toolbar.menu.clear()
            childFragmentManager.top.also { childFragmentManager.popBackStackImmediate() }?.viewController
        }
    }

    fun replace(controller: ViewController, animated: Boolean = true): ViewController? {
        return transaction {
            pop().also {
                push(controller, animated)
            }
        }
    }

    fun replaceAll(controller: ViewController, animated: Boolean = true): List<ViewController> {
        return transaction {
            (0 until childFragmentManager.backStackEntryCount).map { pop() }.filterNotNull().also {
                push(controller, animated)
            }
        } ?: emptyList()
    }

    fun <C : ViewController> push(controller: Observable<C>, animated: Boolean = true) {
        controller
                .subscribeBy(
                        onNext = { push(controller = it, animated = animated) }
                )
                .addTo(lifetimeDisposeBag)
    }

    fun <C : ViewController> replace(controller: Observable<C>, animated: Boolean = true): Observable<ViewController?> {
        val replacedController = ReplaySubject.create<ViewController?>(1)
        controller
                .subscribeBy(
                        onNext = { controller ->
                            replacedController.onNext(replace(controller = controller, animated = animated))
                        },
                        onComplete = { replacedController.onComplete() }
                )
                .addTo(lifetimeDisposeBag)
        return replacedController
    }

    fun <C : ViewController> popAllAndReplace(controller: Observable<C>): Observable<List<ViewController>> {
        //        let transition = CATransition ()
        //        transition.duration = 0.5
        //        transition.type = kCATransitionMoveIn
        //        transition.subtype = kCATransitionFromLeft
        //        transition.timingFunction = CAMediaTimingFunction(name: kCAMediaTimingFunctionEaseInEaseOut)
        //        view.layer.add(transition, forKey: nil)

        return replaceAll(controller = controller, animated = false)
    }

    fun <C : ViewController> replaceAll(controller: Observable<C>, animated: Boolean = true): Observable<List<ViewController>> {
        val oldControllers = ReplaySubject.create<List<ViewController>>(1)
        controller
                .subscribeBy(
                        onNext = { oldControllers.onNext(replaceAll(controller = it, animated = animated)) },
                        onComplete = { oldControllers.onComplete() }
                )
                .addTo(lifetimeDisposeBag)

        return oldControllers
    }

    fun <C : ViewController, T> push(controller: Observable<ControllerWithResult<C, T>>, animated: Boolean = true): Observable<T> {
        val sharedController = controller.replay(1).refCount()

        sharedController.map { it.controller }
                .subscribeBy(
                        onNext = { push(controller = it, animated = animated) }
                )
                .addTo(lifetimeDisposeBag)

        return sharedController.flatMap { it.result }
    }

    private fun <T> transaction(transaction: () -> T?): T? {
        if (initialized) {
            return transaction()
        } else {
            transactionsMadeBeforeInitialization.add({ transaction() })
            return null
        }
    }

    private fun invalidateBarsVisibility(controller: ViewController?) {
        toolbar.visibility = if (controller?.prefersHiddenToolbar == false) View.VISIBLE else View.GONE
        tabBarController?.tabBar?.visibility = if (controller?.hidesBottomBarWhenPushed == true) View.GONE else View.VISIBLE
    }
}
