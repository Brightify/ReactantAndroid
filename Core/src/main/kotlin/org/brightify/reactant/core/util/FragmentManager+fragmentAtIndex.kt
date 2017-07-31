package org.brightify.reactant.core.util

import android.app.FragmentManager
import org.brightify.reactant.core.ViewControllerWrapper

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal val FragmentManager.top: ViewControllerWrapper?
    get() = getFragmentAtIndex(backStackEntryCount - 1)

internal fun FragmentManager.getFragmentAtIndex(index: Int): ViewControllerWrapper? {
    return if (index in 0..(backStackEntryCount - 1)) findFragmentByTag(getBackStackEntryAt(index).name) as? ViewControllerWrapper else null
}
