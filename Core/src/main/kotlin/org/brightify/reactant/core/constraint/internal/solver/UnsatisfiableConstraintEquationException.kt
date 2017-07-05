package org.brightify.reactant.core.constraint.internal.solver

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class UnsatisfiableConstraintEquationException(equation: Equation) : RuntimeException("Equation ($equation) cannot be satisfied.")