package org.brightify.reactant.core.constraint

import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
val View.snp: ConstraintDsl
    get() = ConstraintDsl(this)
