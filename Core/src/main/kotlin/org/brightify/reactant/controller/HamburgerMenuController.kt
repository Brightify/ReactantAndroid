package org.brightify.reactant.controller

import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.controller.util.TransactionManager

/**
 * @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
// TODO Refactor common code from TabBarController
open class HamburgerMenuController(private val viewControllers: List<ViewController>): ViewController() {

    val navigationView: NavigationView
        get() = navigationView_ ?: viewNotLoadedError()

    val drawer: DrawerLayout
        get() = view as DrawerLayout

    var selectedViewController: ViewController
        get() = displayedViewController
        set(value) {
            if (selectedViewController != value) {
                transactionManager.transaction {
                    clearLayout(true)
                    showViewController(value)
                    displayedViewController.viewDidAppear()
                }
            }
        }

    var selectedViewControllerIndex: Int
        get() = (0 until navigationView.menu.size()).firstOrNull { navigationView.menu.getItem(it).isChecked } ?: 0
        set(value) {
            selectedViewController = viewControllers[value]
        }

    private var layout: FrameLayout? = null
    private var navigationView_: NavigationView? = null

    private var displayedViewController: ViewController = viewControllers[0]
    private val transactionManager = TransactionManager()

    override fun activityChanged() {
        super.activityChanged()

        viewControllers.forEach {
            it.activity_ = activity_
        }
    }

    override fun loadView() {
        super.loadView()

        layout = FrameLayout(activity)
        navigationView_ = NavigationView(activity)

        view_ = DrawerLayout(activity).children(layout, navigationView)

        view.fitsSystemWindows = true

        navigationView.fitsSystemWindows = true
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout?.layoutParams = DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        navigationView.layoutParams =
                DrawerLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
                    gravity = Gravity.START
                }

        viewControllers.forEach {
            it.hamburgerMenuController = this
            updateMenuItem(it)
        }

        transactionManager.enabled = true

        // Fix shrinking of layout when both keyboard and navigationView is visible.
        navigationView.getChildAt(0).isScrollContainer = false
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        transactionManager.transaction {
            clearLayout(false)
            showViewController(selectedViewController)
        }
    }

    override fun viewDidAppear() {
        super.viewDidAppear()

        displayedViewController.viewDidAppear()
    }

    override fun viewWillDisappear() {
        super.viewWillDisappear()

        displayedViewController.viewWillDisappear()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()

        displayedViewController.viewDidDisappear()
    }

    override fun viewDestroyed() {
        super.viewDestroyed()

        transactionManager.enabled = false
        layout = null
        navigationView_ = null
    }

    override fun deactivated() {
        super.deactivated()

        viewControllers.forEach {
            it.activity_ = null
        }
    }

    override fun onBackPressed(): Boolean = displayedViewController.onBackPressed()

    override fun destroyViewHierarchy() {
        super.destroyViewHierarchy()

        viewControllers.forEach {
            it.destroyViewHierarchy()
        }
    }

    fun updateMenuItem(viewController: ViewController) {
        val index = viewControllers.indexOf(viewController)
        navigationView.menu.removeItem(index)

        val text = viewController.hamburgerMenuItem?.titleRes?.let { activity.getString(it) } ?: "Undefined"
        val item = navigationView.menu.add(Menu.NONE, index, index, text)
        item.isCheckable = true
        viewController.hamburgerMenuItem?.imageRes?.let { item.setIcon(it) }

        item.setOnMenuItemClickListener {
            selectedViewController = viewControllers[item.itemId]
            drawer.closeDrawer(navigationView)
            return@setOnMenuItemClickListener false
        }
    }

    fun invalidateChild() {
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    private fun clearLayout(callCallbacks: Boolean) {
        if (callCallbacks) {
            displayedViewController.viewWillDisappear()
        }
        layout?.removeAllViews()
        if (callCallbacks) {
            displayedViewController.viewDidDisappear()
        }
    }

    private fun showViewController(viewController: ViewController) {
        displayedViewController = viewController
        navigationView.setCheckedItem(viewControllers.indexOf(viewController))
        displayedViewController.hamburgerMenuController = this
        displayedViewController.viewWillAppear()
        addViewToHierarchy()
    }

    private fun addViewToHierarchy() {
        layout?.addView(displayedViewController.view)
    }
}