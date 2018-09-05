package org.brightify.reactant.autolayout.exception

import android.view.View
import org.brightify.reactant.autolayout.util.description

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class WrongParentException(view: View) : RuntimeException("View (${view.description})'s parent is not an AutoLayout.")