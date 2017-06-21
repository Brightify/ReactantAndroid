package org.brightify.reactant.core.constraint.util

import android.view.View
import org.brightify.reactant.core.constraint.dsl.ConstraintDsl

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
val View.snp: ConstraintDsl
    get() = ConstraintDsl(this)

val View.description: String
    get() = "${javaClass.simpleName}($id)"
