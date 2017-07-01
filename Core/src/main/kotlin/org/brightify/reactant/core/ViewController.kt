package org.brightify.reactant.core

import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewController {

    val activity: ReactantActivity
        get() = viewControllerWrapper.activity!!

    var navigationController: NavigationController? = null

    lateinit var contentView: View
        internal set

    internal lateinit var viewControllerWrapper: ViewControllerWrapper

    /**
     * Returns true if event is handled.
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    open fun onCreate() {
        contentView = View(activity)
    }

    open fun onActivityCreated() {
    }

    open fun onStart() {
    }

    open fun onResume() {
    }

    open fun onPause() {
    }

    open fun onStop() {
    }

    fun present(controller: ViewController, animated: Boolean = true) {
        activity.present(controller, animated)
    }

    fun dismiss(animated: Boolean = true) {
        activity.dismiss(animated)
    }
}