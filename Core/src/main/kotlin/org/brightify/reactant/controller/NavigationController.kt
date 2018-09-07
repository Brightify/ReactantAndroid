package org.brightify.reactant.controller

import android.graphics.Color
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.FrameLayout
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
open class NavigationController(
        private val initialController: ViewController? = null,
        private val toolbarTheme: Int? = null): ViewController() {

    var isNavigationBarHidden: Boolean by onChange(false) { _, _, _ ->
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    var toolbar: Toolbar? = null
        private set

    private var layout: AutoLayout? = null
    private var layoutContent: FrameLayout? = null
    private val viewControllerStack = Stack<ViewController>()
    private val toolbarHeight = 56 // FIXME get correct value
    private val transactionManager = TransactionManager()

    override fun activityChanged() {
        super.activityChanged()

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

        initialController?.navigationController = this
        initialController?.let { viewControllerStack.push(it) }

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
        if (viewControllerStack.peek().onBackPressed()) {
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
            viewController.activity_ = null
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
            old.activity_ = null
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
            viewControllerStack.peek().viewWillDisappear()
        }
        layoutContent?.removeAllViews()
        (view as ViewGroup).removeAllViews()
        if (callCallbacks) {
            viewControllerStack.peek().viewDidDisappear()
        }
    }

    private fun showViewController() {
        resetViewControllerSpecificSettings()
        viewControllerStack.peek().activity_ = activity_
        viewControllerStack.peek().navigationController = this
        viewControllerStack.peek().viewWillAppear()

        val shouldHideBottomBar = viewControllerStack
                .map { it.hidesBottomBarWhenPushed }
                .reduce { accumulator, hidesBottomBarWhenPushed ->
                    accumulator || hidesBottomBarWhenPushed
                }
        tabBarController?.setTabBarHidden(shouldHideBottomBar)
        addViewToHierarchy()
    }

    private fun addViewToHierarchy() {
        if (isNavigationBarHidden) {
            (view as ViewGroup).addView(viewControllerStack.peek().view)
        } else {
            layoutContent?.addView(viewControllerStack.peek().view)
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
