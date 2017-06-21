package org.brightify.reactant.core.constraint.exception

import org.brightify.reactant.core.constraint.Constraint

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class UnsatisfiableConstraintException internal constructor(constraint: Constraint) : RuntimeException(
        "Constraint ($constraint) cannot be satisfied.")
