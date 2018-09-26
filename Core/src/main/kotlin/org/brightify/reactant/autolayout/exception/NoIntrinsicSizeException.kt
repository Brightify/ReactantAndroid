package org.brightify.reactant.autolayout.exception

import android.view.View
import org.brightify.reactant.autolayout.util.description

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class NoIntrinsicSizeException(view: View): RuntimeException("AutoLayout (${view.description}) does not have intrinsic size.")