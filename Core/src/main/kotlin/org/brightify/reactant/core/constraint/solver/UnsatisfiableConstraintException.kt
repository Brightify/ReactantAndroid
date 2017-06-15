package org.brightify.reactant.core.constraint.solver

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class UnsatisfiableConstraintException internal constructor(equation: Equation) : RuntimeException(equation.toString())
