package org.brightify.reactant.core.constraint.solver

import org.brightify.reactant.core.constraint.ConstraintVariable

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class Term(val coefficient: Number, val variable: ConstraintVariable) {

    constructor(variable: ConstraintVariable): this(1, variable)
}
