package org.brightify.reactant.controller

import android.support.design.widget.BottomNavigationView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
open class TabBarController(private val viewControllers: List<ViewController>) : ViewController() {

    lateinit var tabBar: BottomNavigationView
        private set

    var isTabBarHidden: Boolean by onChange(false) { _, _, _ ->
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    var selectedViewController: ViewController?
        get() = displayedViewController
        set(value) {
            if (selectedViewController != value) {
                transactionManager.transaction {
                    clearLayout(true)
                    value?.let { showViewController(value) }
                    displayedViewController?.viewDidAppear()
                }
            }
        }

    var selectedViewControllerIndex: Int
        get() = tabBar.selectedItemId
        set(value) {
            tabBar.selectedItemId = value
        }

    private lateinit var layoutContent: FrameLayout
    private lateinit var layout: AutoLayout

    private var displayedViewController: ViewController? = null
    private val transactionManager = TransactionManager()

    override fun activityChanged() {
        super.activityChanged()

        viewControllers.forEach {
            it.activity_ = activity_
        }
    }

    override fun loadView() {
        super.loadView()

        tabBar = BottomNavigationView(activity)

        layoutContent = FrameLayout(activity)
        layout = AutoLayout(activity)
        view = FrameLayout(activity).children(layout)

        layout.children(layoutContent, tabBar)
        layoutContent.snp.makeConstraints {
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
            showViewController(viewControllers[tabBar.selectedItemId])
        }
    }

    override fun viewDidAppear() {
        super.viewDidAppear()

        displayedViewController?.viewDidAppear()
    }

    override fun viewWillDisappear() {
        super.viewWillDisappear()

        displayedViewController?.viewWillDisappear()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()

        displayedViewController?.viewDidDisappear()
    }

    override fun deactivated() {
        super.deactivated()

        viewControllers.forEach {
            it.activity_ = activity_
        }
    }

    override fun onBackPressed(): Boolean = displayedViewController?.onBackPressed() == true

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
            displayedViewController?.viewWillDisappear()
        }
        layoutContent.removeAllViews()
        (view as ViewGroup).removeAllViews()
        if (callCallbacks) {
            displayedViewController?.viewDidDisappear()
        }
    }

    private fun showViewController(viewController: ViewController) {
        displayedViewController = viewController
        tabBar.selectedItemId = viewControllers.indexOf(viewController)
        displayedViewController?.tabBarController = this
        displayedViewController?.viewWillAppear()
        addViewToHierarchy()
    }

    private fun addViewToHierarchy() {
        if (isTabBarHidden) {
            (view as ViewGroup).addView(displayedViewController?.view)
        } else {
            layoutContent.addView(displayedViewController?.view)
            (view as ViewGroup).addView(layout)
        }
    }
}
