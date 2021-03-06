package org.brightify.reactant.controller

import android.app.Activity
import android.graphics.Color
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import org.brightify.reactant.R
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.controller.util.TransactionManager
import org.brightify.reactant.core.util.onChange
import java.util.Stack

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class NavigationController(initialController: ViewController? = null, private val toolbarTheme: Int? = null): ViewController() {

    var isNavigationBarHidden: Boolean by onChange(false) { _, _, _ ->
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    var toolbar: Toolbar? = null
        private set

    val topViewController: ViewController?
        get() = if (viewControllerStack.isEmpty()) null else viewControllerStack.peek()

    private var layout: AutoLayout? = null
    private var layoutContent: FrameLayout? = null
    private val viewControllerStack = Stack<ViewController>()
    private val toolbarHeight = 56 // FIXME get correct value
    private val transactionManager = TransactionManager()

    init {
        initialController?.let {
            viewControllerStack.push(it)
            it.navigationController = this
        }
    }

    override fun activityDidChange(oldActivity: Activity?) {
        super.activityDidChange(oldActivity)

        viewControllerStack.forEach {
            it.activity_ = activity_
        }
    }

    override fun loadView() {
        super.loadView()

        toolbar = if (toolbarTheme != null) {
            Toolbar(ContextThemeWrapper(activity, toolbarTheme))
        } else {
            null
        }

        layout = AutoLayout(activity)
        layoutContent = FrameLayout(activity)

        view = FrameLayout(activity)
        view.setBackgroundColor(getWindowBackgroundColor())

        layout?.children(toolbar, layoutContent)

        toolbar?.snp?.makeConstraints {
            top.left.right.equalToSuperview()
            height.equalTo(toolbarHeight)
        }

        layoutContent?.snp?.makeConstraints {
            if (toolbar != null) {
                toolbar?.let { top.equalTo(it.snp.bottom) }
            } else {
                top.equalToSuperview()
            }
            bottom.left.right.equalToSuperview()
        }

        transactionManager.enabled = true
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

        topViewController?.viewDidAppear()
    }

    override fun viewWillDisappear() {
        super.viewWillDisappear()

        topViewController?.viewWillDisappear()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()

        topViewController?.viewDidDisappear()
    }

    override fun viewDestroyed() {
        super.viewDestroyed()

        transactionManager.enabled = false
        toolbar = null
        layout = null
        layoutContent = null
    }

    override fun deactivated() {
        super.deactivated()

        viewControllerStack.forEach {
            it.activity_ = null
        }
    }

    override fun onBackPressed(): Boolean {
        if (topViewController?.onBackPressed() == true) {
            return true
        }

        return if (viewControllerStack.size > 1) {
            pop()
            true
        } else {
            false
        }
    }

    override fun destroyViewHierarchy() {
        super.destroyViewHierarchy()

        viewControllerStack.forEach {
            it.destroyViewHierarchy()
        }
    }

    fun setNavigationBarHidden(hidden: Boolean, animated: Boolean = true) {
        isNavigationBarHidden = hidden
    }

    fun push(viewController: ViewController, animated: Boolean = true) {
        transactionManager.transaction {
            clearLayout(true)
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
            topViewController?.viewDidAppear()
            viewController.activity_ = null
            return@transaction viewController
        }
    }

    fun replace(viewController: ViewController, animated: Boolean = true): ViewController? {
        return transactionManager.transaction {
            clearLayout(true)
            val old = if (viewControllerStack.isEmpty()) null else viewControllerStack.pop()
            viewControllerStack.push(viewController)
            showViewController()
            topViewController?.viewDidAppear()
            old?.activity_ = null
            return@transaction old
        }
    }

    fun replaceAll(viewController: ViewController, animated: Boolean = true): List<ViewController> {
        return transactionManager.transaction {
            clearLayout(true)
            val viewControllers = viewControllerStack.elements().toList()
            viewControllerStack.clear()
            viewControllerStack.push(viewController)
            showViewController()
            viewController.viewDidAppear()
            viewControllers.forEach { it.activity_ = null }
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
            topViewController?.viewWillDisappear()
        }
        layoutContent?.removeAllViews()
        (view as ViewGroup).removeAllViews()
        if (callCallbacks) {
            topViewController?.viewDidDisappear()
        }
    }

    private fun showViewController() {
        resetViewControllerSpecificSettings()
        topViewController?.activity_ = activity_
        topViewController?.navigationController = this
        topViewController?.viewWillAppear()

        val shouldHideBottomBar = viewControllerStack
                .map { it.hidesBottomBarWhenPushed }
                .fold(false) { accumulator, hidesBottomBarWhenPushed ->
                    accumulator || hidesBottomBarWhenPushed
                }
        tabBarController?.setTabBarHidden(shouldHideBottomBar)
        addViewToHierarchy()
    }

    private fun addViewToHierarchy() {
        if (isNavigationBarHidden) {
            topViewController?.view?.let { (view as ViewGroup).addView(it) }
        } else {
            topViewController?.view?.let { layoutContent?.addView(it) }
            (view as ViewGroup).addView(layout)
        }
    }

    private fun getWindowBackgroundColor(): Int {
        val a = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.windowBackground, a, true)
        return if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            a.data
        } else {
            // windowBackground is not a color, probably a drawable
            // FIXME solve window background drawables - not very often used in our apps
            ///val d = activity.getResources().getDrawable(a.resourceId)
            Color.WHITE
        }
    }

    private fun resetViewControllerSpecificSettings() {
        toolbar?.navigationIcon = if (viewControllerStack.size > 1) {
            toolbar?.context?.getDrawable(R.drawable.abc_ic_ab_back_material)
        } else {
            null
        }
        toolbar?.setNavigationOnClickListener {
            // TODO We should propagate information about "back type" (navigation | device button)
            onBackPressed()
        }
        toolbar?.menu?.clear()
        isNavigationBarHidden = false
    }
}
