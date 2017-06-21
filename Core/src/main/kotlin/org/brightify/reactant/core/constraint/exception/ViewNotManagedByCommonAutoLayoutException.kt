package org.brightify.reactant.core.constraint.exception

import android.view.View
import org.brightify.reactant.core.constraint.description

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ViewNotManagedByCommonAutoLayoutException(first: View, second: View) : RuntimeException(
        "Views (${first.description}) and (${second.description}) are not managed by common AutoLayout.")