package org.brightify.reactant.core

import android.app.FragmentManager
import android.app.FragmentTransaction
import android.support.design.widget.BottomNavigationView
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:matous@brightify.org">Matous Hybl</a>
 */
open class TabBarController(private val controllers: List<ViewController>) : ViewController() {

    lateinit var fragmentContainer: FrameLayout
    lateinit var tabBar: BottomNavigationView

    private val childFragmentManager: FragmentManager
        get() = viewControllerWrapper.childFragmentManager

    private var selectedController: ViewController? = null

    override fun onCreate() {
        fragmentContainer = FrameLayout(activity)
        fragmentContainer.assignId()

        tabBar = BottomNavigationView(activity)
        val layout = AutoLayout(activity)
        contentView = layout
        contentView.assignId()
        contentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        fragmentContainer.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        layout.addView(fragmentContainer)
        layout.addView(tabBar)
        fragmentContainer.snp.makeConstraints {
            left.right.top.equalToSuperview()
            bottom.equalTo(tabBar.snp.top)
        }

        tabBar.snp.verticalContentHuggingPriority = ConstraintPriority.required
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
                if(tabBar.selectedItemId != item.itemId) {
                    displayController(controller)
                } else {
                    // FIXME pop backstack - check guidelines for correct behavior
                }
                return@setOnMenuItemClickListener false
            }
        }
        selectedController = controllers.firstOrNull()
    }

    override fun onActivityCreated() {
        super.onActivityCreated()

        selectedController?.let { displayController(it) }
    }

    private fun displayController(controller: ViewController, animated: Boolean = false) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(fragmentContainer.id, controller.viewControllerWrapper)
        transaction.addToBackStack(null)
        transaction.setTransition(if (animated) FragmentTransaction.TRANSIT_FRAGMENT_OPEN else FragmentTransaction.TRANSIT_NONE)
        transaction.commit()
        controller.tabBarController = this
        selectedController = controller
    }
}

data class TabBarItem(val titleRes: Int, val imageRes: Int)