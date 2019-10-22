package org.brightify.reactant.controller

import com.google.android.material.bottomnavigation.BottomNavigationView
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

    private val layoutContent = FrameLayout(ReactantActivity.context)
    private val layout = AutoLayout(ReactantActivity.context)
    private var displayedViewController: ViewController? = null
    private val transactionManager = TransactionManager()

    init {
        view = FrameLayout(ReactantActivity.context).children(layout)
    }

    override fun init() {
        super.init()

        loadViewIfNeeded()
    }

    override fun loadView() {
        super.loadView()

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
            addChildContainer(it)
            it.tabBarController = this
            updateTabBarItem(it)
            it.loadViewIfNeeded()
        }

        transactionManager.enabled = true
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        tabBar.snp.setVerticalIntrinsicSizePriority(ConstraintPriority.required)
        tabBar.visibility = View.VISIBLE
        ReactantActivity.instance.beforeKeyboardVisibilityChanged.subscribe {
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

    override fun onBackPressed(): Boolean = displayedViewController?.onBackPressed() == true

    fun updateTabBarItem(viewController: ViewController) {
        val index = viewControllers.indexOf(viewController)
        tabBar.menu.removeItem(index)

        val text = viewController.tabBarItem?.titleRes?.let { resources.getString(it) } ?: "Undefined"
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
