package org.brightify.reactant.core.constraint.internal.manager

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class DelegatedConstraintManager(val manager: ConstraintManager) : ConstraintManager by manager
