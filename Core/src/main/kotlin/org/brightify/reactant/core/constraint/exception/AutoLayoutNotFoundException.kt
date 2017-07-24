package org.brightify.reactant.core.constraint.exception

import android.view.View
import org.brightify.reactant.core.constraint.util.description

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class AutoLayoutNotFoundException(view: View) : RuntimeException("View (${view.description} must be AutoLayout or child of AutoLayout.")