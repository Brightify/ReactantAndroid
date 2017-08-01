package org.brightify.reactant.autolayout.util

import android.view.View
import org.brightify.reactant.autolayout.dsl.ConstraintDsl

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
val View.snp: ConstraintDsl
    get() = ConstraintDsl(this)

val View.description: String
    get() = "${javaClass.simpleName}($id)"
