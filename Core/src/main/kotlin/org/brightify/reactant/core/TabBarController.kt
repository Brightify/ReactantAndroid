package org.brightify.reactant.core

import android.app.FragmentManager
import android.app.FragmentTransaction
import android.support.design.widget.BottomNavigationView
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout

/**
 *  @author <a href="mailto:matous@brightify.org">Matous Hybl</a>
 */
open class TabBarController(private val controllers: List<ViewController>) : ViewController() {

    lateinit var fragmentContainer: FrameLayout
    lateinit var tabBar: BottomNavigationView

    private val childFragmentManager: FragmentManager
        get() = viewControllerWrapper.childFragmentManager

    override fun onCreate() {
        val layout = RelativeLayout(activity)
        contentView = layout
        fragmentContainer = FrameLayout(activity)
        tabBar = BottomNavigationView(activity)

        fragmentContainer.assignId()
        tabBar.assignId()

        layout.addView(fragmentContainer)
        layout.addView(tabBar)

        val contentViewParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        contentViewParams.addRule(RelativeLayout.ABOVE, tabBar.id)
        contentView.layoutParams = contentViewParams

        val tabBarParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tabBarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        tabBar.layoutParams = tabBarParams

    }

    override fun onActivityCreated() {
        super.onActivityCreated()

        controllers.forEachIndexed { index, controller ->
            val text = controller.tabBarItem?.titleRes?.let { activity.resources.getString(it) } ?: "Undefined"
            val item = tabBar.menu.add(Menu.NONE, index, 0, text)
            val imageRes = controller.tabBarItem?.imageRes
            if (imageRes != null) {
                item.icon = activity.resources.getDrawable(imageRes)
            }
            item.setOnMenuItemClickListener {
                if(tabBar.selectedItemId != item.itemId) {
                    displayController(controller)
                } else {
                    // FIXME pop backstack - check guidelines for correct behavior
                }
                return@setOnMenuItemClickListener false
            }
        }
        val controller = controllers.firstOrNull()
        if (controller != null) {
            displayController(controller)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun displayController(controller: ViewController, animated: Boolean = false) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(fragmentContainer.id, ViewControllerWrapper(controller))
        transaction.addToBackStack(null)
        transaction.setTransition(if (animated) FragmentTransaction.TRANSIT_FRAGMENT_OPEN else FragmentTransaction.TRANSIT_NONE)
        transaction.commit()
        controller.tabBarController = this
    }
}

data class TabBarItem(val titleRes: Int, val imageRes: Int)