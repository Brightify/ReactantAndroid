package org.brightify.reactant.core.constraint.exception

import android.view.View
import org.brightify.reactant.core.constraint.description

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class AutoLayoutNotFoundException(view: View): RuntimeException("AutoLayout is not present in view's (${view.description}) hierarchy.")