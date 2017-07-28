package org.brightify.reactant.core.constraint.dsl

import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.CollapseAxis
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.exception.AutoLayoutNotFoundException
import org.brightify.reactant.core.constraint.exception.NoIntrinsicSizeException
import org.brightify.reactant.core.constraint.internal.ConstraintManager
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.view.VisibilityManager
import org.brightify.reactant.core.constraint.internal.view.IntrinsicSizeManager
import org.brightify.reactant.core.constraint.util.children
import org.brightify.reactant.core.constraint.util.description
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ConstraintDsl internal constructor(private val view: View) {

    val left: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.left)

    val top: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.top)

    val right: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.right)

    val bottom: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.bottom)

    val width: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.width)

    val height: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.height)

    // TODO Add support for RTL layouts
    val leading: ConstraintVariable
        get() = left

    val trailing: ConstraintVariable
        get() = right

    val centerX: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.centerX)

    val centerY: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.centerY)

    var collapsaAxis: CollapseAxis
        get() = visibilityManager.collapseAxis
        set(value) {
            visibilityManager.collapseAxis = value
        }

    var horizontalContentHuggingPriority: ConstraintPriority
        get() = intrinsicSizeManager.width.contentHuggingPriority
        set(value) {
            intrinsicSizeManager.width.contentHuggingPriority = value
        }

    var verticalContentHuggingPriority: ConstraintPriority
        get() = intrinsicSizeManager.height.contentHuggingPriority
        set(value) {
            intrinsicSizeManager.height.contentHuggingPriority = value
        }

    var horizontalContentCompressionResistancePriority: ConstraintPriority
        get() = intrinsicSizeManager.width.contentCompressionResistancePriority
        set(value) {
            intrinsicSizeManager.width.contentCompressionResistancePriority = value
        }

    var verticalContentCompressionResistancePriority: ConstraintPriority
        get() = intrinsicSizeManager.height.contentCompressionResistancePriority
        set(value) {
            intrinsicSizeManager.height.contentCompressionResistancePriority = value
        }

    internal var intrinsicWidth: Double
        get() = intrinsicSizeManager.width.size
        set(value) {
            intrinsicSizeManager.width.size = value
        }

    internal var intrinsicHeight: Double
        get() = intrinsicSizeManager.height.size
        set(value) {
            intrinsicSizeManager.height.size = value
        }

    private val constraintManager: ConstraintManager = (view.parent as? AutoLayout ?: view as? AutoLayout)?.constraintManager
            ?: throw AutoLayoutNotFoundException(view)

    private val intrinsicSizeManager: IntrinsicSizeManager
        get() = constraintManager.getIntrinsicSizeManager(view) ?: throw NoIntrinsicSizeException(view)

    private val visibilityManager: VisibilityManager
        get() = constraintManager.getVisibilityManager(view)

    fun makeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        val createdConstraints = ArrayList<Constraint>()
        closure(ConstraintMakerProvider(view, createdConstraints))
        createdConstraints.forEach {
            it.initialized = true
            if (it.isActive) {
                it.activate()
            }
        }
    }

    fun remakeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        constraintManager.removeViewConstraints(view)
        makeConstraints(closure)
    }

    fun disableIntrinsicSize() {
        constraintManager.disableIntrinsicSize(view)
    }

    fun debugValues() {
        val otherValues = if ((view !is AutoLayout)) "\nintrinsicWidth = $intrinsicWidth\n" +
                "intrinsicHeight = $intrinsicHeight\n" +
                "horizontalContentHuggingPriority = $horizontalContentHuggingPriority\n" +
                "verticalContentHuggingPriority = $verticalContentHuggingPriority\n" +
                "horizontalContentCompressionResistancePriority = $horizontalContentCompressionResistancePriority\n" +
                "verticalContentCompressionResistancePriority = $verticalContentCompressionResistancePriority" else ""
        (listOf(top, left, bottom, right, width, height, centerX, centerY)
                .map {
                    val value = constraintManager.getValueForVariable(it)
                    "${it.type} = ${if (value == 0.0) Math.abs(value) else value}"
                }
                .joinToString("\n") + otherValues)
                .let { Log.d("debugValues(view=${view.description})", it) }
    }

    fun debugValuesRecursive() {
        debugValues()
        if (view is AutoLayout) {
            (view as ViewGroup).children.forEach { it.snp.debugValuesRecursive() }
        }
    }

    fun debugConstraints() {
        constraintManager.allConstraints
                .flatMap { it.constraintItems }
                .filter { it.leftVariable.view == view || it.rightVariable?.view == view }
                .map { it.toString() }
                .joinToString("\n").let { Log.d("debugConstraints(view=${view.description})", it) }
    }

    fun debugConstraintsRecursive() {
        debugConstraints()
        if (view is AutoLayout) {
            (view as ViewGroup).children.forEach { it.snp.debugConstraintsRecursive() }
        }
    }

    internal fun addConstraint(constraint: Constraint) {
        constraintManager.addConstraint(constraint)
    }

    internal fun removeConstraint(constraint: Constraint) {
        constraintManager.removeConstraint(constraint)
    }

    private fun ConstraintVariable(type: ConstraintType): ConstraintVariable = ConstraintVariable(view, type)
}
