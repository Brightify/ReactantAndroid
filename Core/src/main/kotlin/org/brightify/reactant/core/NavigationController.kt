package org.brightify.reactant.core

import android.app.FragmentManager
import android.app.FragmentTransaction
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class NavigationController : ViewController() {

    private val transactionsMadeBeforeInitialization = ArrayList<() -> Unit>()
    private var initialized = false

    private val childFragmentManager: FragmentManager
        get() = viewControllerWrapper.childFragmentManager

    override fun onCreate() {
        contentView = FrameLayout(activity)
        contentView.assignId()
        contentView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onActivityCreated() {
        initialized = true
        transactionsMadeBeforeInitialization.forEach { it() }
        transactionsMadeBeforeInitialization.clear()
    }

    override fun onBackPressed(): Boolean {
        if (childFragmentManager.top?.viewController?.onBackPressed() == true) {
            return true
        }

        val stackSize = childFragmentManager.backStackEntryCount
        childFragmentManager.popBackStack()
        return stackSize > 1
    }

    fun push(controller: ViewController, animated: Boolean = true) {
        transaction {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(contentView.id, ViewControllerWrapper(controller))
            transaction.addToBackStack(null)
            transaction.setTransition(if (animated) FragmentTransaction.TRANSIT_FRAGMENT_OPEN else FragmentTransaction.TRANSIT_NONE)
            transaction.commit()
        }
    }

    fun pop(animated: Boolean = true): ViewController? {
        return transaction {
            childFragmentManager.top.also { childFragmentManager.popBackStackImmediate() }?.viewController
        }
    }

    fun replace(controller: ViewController, animated: Boolean = true): ViewController? {
        return transaction {
            pop().also {
                push(controller, animated)
            }
        }
    }

    fun replaceAll(controller: ViewController, animated: Boolean = true): List<ViewController> {
        return transaction {
            (0 until childFragmentManager.backStackEntryCount).map { pop() }.filterNotNull().also {
                push(controller, animated)
            }
        } ?: emptyList()
    }

    private fun <T> transaction(transaction: () -> T?): T? {
        if (initialized) {
            return transaction()
        } else {
            transactionsMadeBeforeInitialization.add({ transaction() })
            return null
        }
    }
}
