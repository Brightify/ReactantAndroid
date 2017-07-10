package org.brightify.reactant.core

import android.app.FragmentManager
import android.app.FragmentTransaction
import android.support.design.widget.BottomNavigationView
import android.view.ViewGroup
import android.widget.FrameLayout
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:matous@brightify.org">Matous Hybl</a>
 */
open class TabBarController(private val controllers: Array<ViewController>) : ViewController() {
    private var initialized = false

    lateinit var fragmentContainer: FrameLayout
    lateinit var tabBar: BottomNavigationView

    private val childFragmentManager: FragmentManager
        get() = viewControllerWrapper.childFragmentManager

    override fun onCreate() {
        super.onCreate()

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

        tabBar.snp.makeConstraints {
            left.equalToSuperview()
            right.equalToSuperview()
            bottom.equalToSuperview()
        }
    }

    override fun onActivityCreated() {
        super.onActivityCreated()
        initialized = true

        controllers.forEach { controller ->
            val item = tabBar.menu.add(controller.tabBarItem?.title ?: "Undefined")
            val imageRes = controller.tabBarItem?.imageRes
            if (imageRes != null) {
                item.icon = activity.resources.getDrawable(imageRes)
            }
            item.setOnMenuItemClickListener {
                displayController(controller)
                return@setOnMenuItemClickListener false
            }
        }
        val controller = controllers.firstOrNull()
        if (controller != null) {
            displayController(controller)
        }
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

data class TabBarItem(val title: String, val imageRes: Int)