package org.brightify.reactant.autolayout.internal.solver

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
internal class UnsatisfiableEquationException(equation: Equation) : RuntimeException("Equation ($equation) cannot be satisfied.")