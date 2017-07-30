package org.brightify.reactant.core

import android.app.FragmentManager

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal val FragmentManager.top: ViewControllerWrapper?
    get() = if (backStackEntryCount == 0) null else findFragmentByTag(
            getBackStackEntryAt(backStackEntryCount - 1).name) as? ViewControllerWrapper
