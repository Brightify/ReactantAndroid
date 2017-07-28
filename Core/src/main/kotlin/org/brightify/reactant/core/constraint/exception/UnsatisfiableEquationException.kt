package org.brightify.reactant.core.constraint.exception

import org.brightify.reactant.core.constraint.internal.solver.Equation

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class UnsatisfiableEquationException internal constructor(equation: Equation) : RuntimeException("Equation ($equation) cannot be satisfied.")