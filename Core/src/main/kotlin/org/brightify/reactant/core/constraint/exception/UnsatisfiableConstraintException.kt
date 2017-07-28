package org.brightify.reactant.core.constraint.exception

import org.brightify.reactant.core.constraint.internal.ConstraintItem

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class UnsatisfiableConstraintException internal constructor(constraintItem: ConstraintItem) : RuntimeException(
        "Constraint ($constraintItem) cannot be satisfied.")
