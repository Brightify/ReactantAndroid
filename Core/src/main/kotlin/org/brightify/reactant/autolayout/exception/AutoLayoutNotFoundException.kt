package org.brightify.reactant.autolayout.exception

import android.view.View
import org.brightify.reactant.autolayout.util.description

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class AutoLayoutNotFoundException(view: View): RuntimeException("View (${view.description} must be AutoLayout or child of AutoLayout.")