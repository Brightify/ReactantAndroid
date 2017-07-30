package org.brightify.reactant.core.util

import android.app.Fragment
import android.app.FragmentTransaction
import java.util.UUID

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun FragmentTransaction.push(contentViewId: Int, fragment: Fragment) {
    val tag = fragment.tag ?: UUID.randomUUID().toString()
    addToBackStack(tag)
    replace(contentViewId, fragment, tag)
}