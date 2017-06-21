package org.brightify.reactant.core.constraint.exception

import org.brightify.reactant.core.constraint.solver.Equation

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class UnsatisfiableConstraintEquationException(equation: Equation) : RuntimeException("Equation ($equation) cannot be satisfied.")