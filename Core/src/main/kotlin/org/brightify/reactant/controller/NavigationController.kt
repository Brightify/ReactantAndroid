package org.brightify.reactant.controller

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
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
open class NavigationController(private val initialController: ViewController?): ViewController() {

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

    constructor(): this(null)

    override fun init() {
        super.init()

        loadViewIfNeeded()
    }

    override fun loadView() {
        super.loadView()

        view = FrameLayout(ReactantActivity.context)
        view.setBackgroundColor(getWindowBackgroundColor())

        layout.children(toolbar, layoutContent)

        toolbar.snp.makeConstraints {
            top.left.right.equalToSuperview()
            height.equalTo(toolbarHeight)
        }

        layoutContent.snp.makeConstraints {
            top.equalTo(toolbar.snp.bottom)
            bottom.left.right.equalToSuperview()
        }

        initialController?.let { addChildContainer(it) }
        initialController?.navigationController = this
        initialController?.loadViewIfNeeded()
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
            addChildContainer(viewController)
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
            removeChildContainer(viewController)
            return@transaction viewController
        }
    }

    fun replace(viewController: ViewController, animated: Boolean = true): ViewController? {
        return transactionManager.transaction {
            addChildContainer(viewController)
            clearLayout(!viewControllerStack.empty())
            val old = viewControllerStack.pop()
            viewControllerStack.push(viewController)
            showViewController()
            viewControllerStack.peek().viewDidAppear()
            removeChildContainer(old)
            return@transaction old
        }
    }

    fun replaceAll(viewController: ViewController, animated: Boolean = true): List<ViewController> {
        return transactionManager.transaction {
            addChildContainer(viewController)
            clearLayout(!viewControllerStack.empty())
            val viewControllers = viewControllerStack.elements().toList()
            viewControllerStack.clear()
            viewControllerStack.push(viewController)
            showViewController()
            viewController.viewDidAppear()
            viewControllers.forEach { removeChildContainer(it) }
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
        resetViewControllerSpecificSettings()
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

    private fun getWindowBackgroundColor(): Int {
        val a = TypedValue()
        ReactantActivity.instance.theme.resolveAttribute(android.R.attr.windowBackground, a, true)
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            return a.data
        } else {
            // windowBackground is not a color, probably a drawable
            // FIXME solve window background drawables - not very often used in our apps
            ///val d = activity.getResources().getDrawable(a.resourceId)
            return Color.WHITE
        }
    }

    private fun resetViewControllerSpecificSettings() {
        toolbar.navigationIcon = if (viewControllerStack.size > 1) ContextCompat.getDrawable(ReactantActivity.context,
                R.drawable.abc_ic_ab_back_material) else null
        toolbar.setNavigationOnClickListener { pop() }
        toolbar.menu.clear()
        isNavigationBarHidden = false
    }
}
