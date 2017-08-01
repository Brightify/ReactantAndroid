package org.brightify.reactant.controller

import android.support.design.widget.BottomNavigationView
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.core.ReactantActivity

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class TabBarController(private val viewControllers: List<ViewController>) : ViewController() {

    val tabBar = BottomNavigationView(ReactantActivity.globalContext)

    // TODO
    internal var hideTabBar = false

    private val layoutContent = FrameLayout(ReactantActivity.globalContext)
    private val layout = AutoLayout(ReactantActivity.globalContext)

    private var displayedViewController: ViewController? = null

    init {
        loadViewIfNeeded()
    }

    override fun loadView() {
        super.loadView()

        view = FrameLayout(ReactantActivity.globalContext).children(layout)

        layout.children(layoutContent, tabBar)
        layoutContent.snp.makeConstraints {
            left.right.top.equalToSuperview()
            bottom.equalTo(tabBar.snp.top)
        }
        layoutContent.snp.disableIntrinsicSize()

        tabBar.snp.makeConstraints {
            left.right.bottom.equalToSuperview()
        }

        updateTabBarItems()
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        displayedViewController = null
        clearLayout()
        showViewController()
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
        displayedViewController = null
        (view as ViewGroup).removeAllViews()
        layoutContent.removeAllViews()
    }

    override fun onBackPressed(): Boolean = displayedViewController?.onBackPressed() == true

    fun updateTabBarItems() {
        // TODO Set correct controller
        tabBar.menu.clear()

        viewControllers.forEachIndexed { index, controller ->
            val text = controller.tabBarItem?.titleRes?.let { ReactantActivity.instance.resources.getString(it) } ?: "Undefined"
            val item = tabBar.menu.add(Menu.NONE, index, 0, text)
            val imageRes = controller.tabBarItem?.imageRes
            if (imageRes != null) {
                item.icon = ReactantActivity.instance.resources.getDrawable(imageRes)
            }
            item.setOnMenuItemClickListener {
                if (tabBar.selectedItemId != item.itemId) {
                    clearLayout()
                    showViewController()
                    displayedViewController?.viewDidAppear()
                }
                return@setOnMenuItemClickListener false
            }
        }
    }

    private fun clearLayout() {
        displayedViewController?.viewWillDisappear()
        layoutContent.removeAllViews()
        (view as? ViewGroup)?.removeAllViews()
        displayedViewController?.viewDidDisappear()
    }

    private fun showViewController() {
        displayedViewController = viewControllers[tabBar.selectedItemId]
        displayedViewController?.loadViewIfNeeded()
        displayedViewController?.tabBarController = this
        displayedViewController?.viewWillAppear()
        if (hideTabBar) {
            (view as ViewGroup).addView(displayedViewController?.view)
        } else {
            layoutContent.addView(displayedViewController?.view)
            (view as ViewGroup).addView(layout)
        }
    }
}
