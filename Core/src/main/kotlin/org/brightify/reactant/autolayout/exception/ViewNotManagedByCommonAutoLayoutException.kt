package org.brightify.reactant.autolayout.exception

import android.view.View
import org.brightify.reactant.autolayout.util.description

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class ViewNotManagedByCommonAutoLayoutException(first: View, second: View): RuntimeException(
        "Views (${first.description}) and (${second.description}) are not managed by common AutoLayout.")