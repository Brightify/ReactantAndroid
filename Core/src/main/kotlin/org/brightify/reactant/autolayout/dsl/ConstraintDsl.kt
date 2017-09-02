package org.brightify.reactant.autolayout.dsl

import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.CollapseAxis
import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintPriority
import org.brightify.reactant.autolayout.ConstraintVariable
import org.brightify.reactant.autolayout.exception.AutoLayoutNotFoundException
import org.brightify.reactant.autolayout.exception.NoIntrinsicSizeException
import org.brightify.reactant.autolayout.internal.ConstraintManager
import org.brightify.reactant.autolayout.internal.ConstraintType
import org.brightify.reactant.autolayout.internal.view.IntrinsicSizeManager
import org.brightify.reactant.autolayout.internal.view.VisibilityManager
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.description
import org.brightify.reactant.autolayout.util.snp

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

    var collapseAxis: CollapseAxis
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

    private val constraintManager: ConstraintManager = (view as? AutoLayout ?: view.parent as? AutoLayout)?.constraintManager
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
        constraintManager.removeUserViewConstraints(view)
        makeConstraints(closure)
    }

    fun setHorizontalIntrinsicSizePriority(priority: ConstraintPriority) {
        horizontalContentHuggingPriority = priority
        horizontalContentCompressionResistancePriority = priority
    }

    fun setVerticalIntrinsicSizePriority(priority: ConstraintPriority) {
        verticalContentHuggingPriority = priority
        verticalContentCompressionResistancePriority = priority
    }

    fun debugValues() {
        val values = listOf(top, left, bottom, right, width, height, centerX, centerY)
                .map {
                    val value = constraintManager.getValueForVariable(it)
                    "${it.type} = ${if (value == 0.0) Math.abs(value) else value}"
                }
                .joinToString("\n")
        val intrinsicWidth = if (constraintManager.needsIntrinsicWidth(view))
            "\nintrinsicWidth = $intrinsicWidth\n" +
                    "horizontalContentHuggingPriority = $horizontalContentHuggingPriority\n" +
                    "horizontalContentCompressionResistancePriority = $horizontalContentCompressionResistancePriority" else ""
        val intrinsicHeight = if (constraintManager.needsIntrinsicHeight(view))
            "\nintrinsicHeight = $intrinsicHeight\n" +
                    "verticalContentHuggingPriority = $verticalContentHuggingPriority\n" +
                    "verticalContentCompressionResistancePriority = $verticalContentCompressionResistancePriority" else ""
        Log.d("debugValues(view=${view.description})", values + intrinsicWidth + intrinsicHeight)
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
