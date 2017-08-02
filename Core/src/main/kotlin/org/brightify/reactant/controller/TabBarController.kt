package org.brightify.reactant.controller

import android.support.design.widget.BottomNavigationView
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.controller.util.TransactionManager
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.util.onChange

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class TabBarController(private val viewControllers: List<ViewController>) : ViewController() {

    val tabBar = BottomNavigationView(ReactantActivity.context)

    var isTabBarHidden: Boolean by onChange(false) { _, _, _ ->
        if (!transactionManager.isInTransaction) {
            clearLayout(false)
            addViewToHierarchy()
        }
    }

    private val layoutContent = FrameLayout(ReactantActivity.context)
    private val layout = AutoLayout(ReactantActivity.context)
    private var displayedViewController: ViewController? = null
    private val transactionManager = TransactionManager()

    init {
        loadViewIfNeeded()
    }

    override fun loadView() {
        super.loadView()

        view = FrameLayout(ReactantActivity.context).children(layout)

        layout.children(layoutContent, tabBar)
        layoutContent.snp.makeConstraints {
            left.right.top.equalToSuperview()
            bottom.equalTo(tabBar.snp.top)
        }
        layoutContent.snp.disableIntrinsicSize()

        tabBar.snp.makeConstraints {
            left.right.bottom.equalToSuperview()
        }

        viewControllers.forEach {
            it.tabBarController = this
            updateTabBarItem(it)
            it.loadViewIfNeeded()
        }
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

    override fun onBackPressed(): Boolean = displayedViewController?.onBackPressed() == true

    fun updateTabBarItem(viewController: ViewController) {
        val index = viewControllers.indexOf(viewController)
        tabBar.menu.removeItem(index)

        val text = viewController.tabBarItem?.titleRes?.let { resources.getString(it) } ?: "Undefined"
        val item = tabBar.menu.add(Menu.NONE, index, 0, text)
        viewController.tabBarItem?.imageRes?.let { item.icon = resources.getDrawable(it) }

        item.setOnMenuItemClickListener {
            if (tabBar.selectedItemId != item.itemId) {
                transactionManager.transaction {
                    clearLayout(true)
                    showViewController(item.itemId)
                    displayedViewController?.viewDidAppear()
                }
            }
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

    private fun showViewController(index: Int = tabBar.selectedItemId) {
        displayedViewController = viewControllers[index]
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
