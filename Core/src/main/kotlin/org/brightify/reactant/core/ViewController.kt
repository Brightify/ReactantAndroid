package org.brightify.reactant.core

import android.app.Fragment
import java.util.Stack

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewController : Fragment() {

    val activity: ReactantActivity
        get() = getActivity() as ReactantActivity

    private var presentedModalViewControllers = Stack<ViewController>()

    var navigationController: NavigationController? = null

    /**
     * Returns true if event is handled.
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    fun present(controller: ViewController, animated: Boolean = true) {
        activity.present(controller, animated)
    }

    fun dismiss(animated: Boolean = true) {
        activity.dismiss(animated)
    }
}