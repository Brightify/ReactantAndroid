package org.brightify.reactant.core

import android.app.FragmentManager
import android.app.FragmentTransaction
import android.support.design.widget.BottomNavigationView
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.util.snp
import org.brightify.reactant.core.util.push
import org.brightify.reactant.core.util.top

/**
 *  @author <a href="mailto:matous@brightify.org">Matous Hybl</a>
 */
open class TabBarController(private val controllers: List<ViewController>) : ViewController() {

    lateinit var fragmentContainer: FrameLayout
    lateinit var tabBar: BottomNavigationView

    private val backStackChangeListener = {
        val viewController = childFragmentManager.top?.viewController
        if (viewController !is NavigationController) {
            tabBarController?.tabBar?.visibility =
                    if (viewController?.hidesBottomBarWhenPushed == true) View.GONE else View.VISIBLE
        }
    }

    private val childFragmentManager: FragmentManager
        get() = viewControllerWrapper.childFragmentManager

    override fun onCreate() {
        fragmentContainer = FrameLayout(activity)
        fragmentContainer.assignId()

        tabBar = BottomNavigationView(activity)
        val layout = AutoLayout(activity)
        contentView = layout
        contentView.assignId()
        contentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.addView(fragmentContainer)
        layout.addView(tabBar)
        fragmentContainer.snp.makeConstraints {
            left.right.top.equalToSuperview()
            bottom.equalTo(tabBar.snp.top)
        }
        fragmentContainer.snp.disableIntrinsicSize()

        tabBar.snp.makeConstraints {
            left.equalToSuperview()
            right.equalToSuperview()
            bottom.equalToSuperview()
        }

        controllers.forEachIndexed { index, controller ->
            val text = controller.tabBarItem?.titleRes?.let { activity.resources.getString(it) } ?: "Undefined"
            val item = tabBar.menu.add(Menu.NONE, index, 0, text)
            val imageRes = controller.tabBarItem?.imageRes
            if (imageRes != null) {
                item.icon = activity.resources.getDrawable(imageRes)
            }
            item.setOnMenuItemClickListener {
                if (tabBar.selectedItemId != item.itemId) {
                    displayController(controller)
                }
                return@setOnMenuItemClickListener false
            }
        }
        controllers.firstOrNull()?.let { displayController(it) }
    }

    override fun onResume() {
        super.onResume()

        childFragmentManager.removeOnBackStackChangedListener(backStackChangeListener)
        childFragmentManager.addOnBackStackChangedListener(backStackChangeListener)
        controllers[tabBar.selectedItemId].tabBarController = this
    }

    override fun onBackPressed(): Boolean {
        return childFragmentManager.top?.viewController?.onBackPressed() == true
    }

    private fun displayController(controller: ViewController, animated: Boolean = false) {
        controller.tabBarController = this
        val transaction = childFragmentManager.beginTransaction()
        transaction.push(fragmentContainer.id, controller.viewControllerWrapper)
        transaction.setTransition(if (animated) FragmentTransaction.TRANSIT_FRAGMENT_OPEN else FragmentTransaction.TRANSIT_NONE)
        transaction.commit()
    }
}

data class TabBarItem(val titleRes: Int, val imageRes: Int)