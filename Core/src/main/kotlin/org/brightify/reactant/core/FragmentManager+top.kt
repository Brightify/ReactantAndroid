package org.brightify.reactant.core

import android.app.FragmentManager

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
val FragmentManager.top: ViewController?
    get() = if (backStackEntryCount == 0) null else findFragmentById(getBackStackEntryAt(backStackEntryCount - 1).id) as? ViewController
