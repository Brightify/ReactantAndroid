package org.brightify.reactant.controller

import android.app.Activity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.rxkotlin.addTo
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.ConstraintPriority
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.controller.util.TransactionManager
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class TabBarController(private val viewControllers: List<ViewController>): ViewController() {

    val tabBar: BottomNavigationView
        get() = tabBar_ ?: viewNotLoadedError()

    var isTabBarHidden: Boolean by onChange(false) { _, _, _ ->
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

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
        get() = viewControllers.indexOf(selectedViewController)
        set(value) {
            selectedViewController = viewControllers[value]
        }

    private var tabBar_: BottomNavigationView? = null
    private var layoutContent: FrameLayout? = null
    private var layout: AutoLayout? = null

    private var displayedViewController: ViewController = viewControllers[0]
    private val transactionManager = TransactionManager()

    override fun activityDidChange(oldActivity: Activity?) {
        super.activityDidChange(oldActivity)

        viewControllers.forEach {
            it.activity_ = activity_
        }
    }

    override fun loadView() {
        super.loadView()

        tabBar_ = BottomNavigationView(activity)

        layoutContent = FrameLayout(activity)
        layout = AutoLayout(activity)
        view = FrameLayout(activity).children(layout)

        layout?.children(layoutContent, tabBar)
        layoutContent?.snp?.makeConstraints {
            left.right.top.equalToSuperview()
            bottom.equalTo(tabBar.snp.top)
        }

        tabBar.snp.makeConstraints {
            left.right.bottom.equalToSuperview()
        }
        tabBar.snp.setVerticalIntrinsicSizePriority(ConstraintPriority.required)

        viewControllers.forEach {
            it.tabBarController = this
            updateTabBarItem(it)
        }

        transactionManager.enabled = true
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        tabBar.snp.setVerticalIntrinsicSizePriority(ConstraintPriority.required)
        tabBar.visibility = View.VISIBLE
        activity.beforeKeyboardVisibilityChanged.subscribe {
            if (it) {
                tabBar.visibility = View.GONE
                tabBar.snp.setVerticalIntrinsicSizePriority(ConstraintPriority.low)
            } else {
                tabBar.snp.setVerticalIntrinsicSizePriority(ConstraintPriority.required)
                tabBar.visibility = View.VISIBLE
            }
        }.addTo(visibleDisposeBag)

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
        tabBar_ = null
        layoutContent = null
        layout = null
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

    fun updateTabBarItem(viewController: ViewController) {
        val index = viewControllers.indexOf(viewController)
        tabBar.menu.removeItem(index)

        val text = viewController.tabBarItem?.titleRes?.let { activity.getString(it) } ?: "Undefined"
        val item = tabBar.menu.add(Menu.NONE, index, index, text)
        viewController.tabBarItem?.imageRes?.let { item.setIcon(it) }

        item.setOnMenuItemClickListener {
            selectedViewController = viewControllers[item.itemId]
            return@setOnMenuItemClickListener false
        }
    }

    fun setTabBarHidden(hidden: Boolean, animated: Boolean = true) {
        isTabBarHidden = hidden
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
        layoutContent?.removeAllViews()
        (view as ViewGroup).removeAllViews()
        if (callCallbacks) {
            displayedViewController.viewDidDisappear()
        }
    }

    private fun showViewController(viewController: ViewController) {
        displayedViewController = viewController
        tabBar.selectedItemId = viewControllers.indexOf(viewController)
        displayedViewController.tabBarController = this
        displayedViewController.viewWillAppear()
        addViewToHierarchy()
    }

    private fun addViewToHierarchy() {
        if (isTabBarHidden) {
            (view as ViewGroup).addView(displayedViewController.view)
        } else {
            layoutContent?.addView(displayedViewController.view)
            (view as ViewGroup).addView(layout)
        }
    }
}
