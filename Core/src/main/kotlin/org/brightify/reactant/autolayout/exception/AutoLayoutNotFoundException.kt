package org.brightify.reactant.autolayout.exception

import android.view.View
import org.brightify.reactant.autolayout.util.description

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class AutoLayoutNotFoundException(view: View) : RuntimeException("View (${view.description} must be AutoLayout or child of AutoLayout.")