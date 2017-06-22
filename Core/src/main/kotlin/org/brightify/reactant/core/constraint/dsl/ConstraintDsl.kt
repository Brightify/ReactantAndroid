package org.brightify.reactant.core.constraint.dsl

import android.util.Log
import android.view.View
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.exception.AutoLayoutNotFoundException
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.manager.ConstraintManager
import org.brightify.reactant.core.constraint.internal.util.IntrinsicSize
import org.brightify.reactant.core.constraint.util.Margin
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

    val leftMargin: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.leftMargin)

    val topMargin: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.topMargin)

    val rightMargin: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.rightMargin)

    val bottomMargin: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.bottomMargin)

    val leadingMargin: ConstraintVariable
        get() = leftMargin

    val trailingMargin: ConstraintVariable
        get() = rightMargin

    val centerXWithMargins: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.centerXWithMargins)

    val centerYWithMargins: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.centerYWithMargins)

    var margin: Margin
        get() = Margin(
                constraintManager.getValueForVariable(ConstraintVariable(ConstraintType.topMarginSize)),
                constraintManager.getValueForVariable(ConstraintVariable(ConstraintType.leftMarginSize)),
                constraintManager.getValueForVariable(ConstraintVariable(ConstraintType.bottomMarginSize)),
                constraintManager.getValueForVariable(ConstraintVariable(ConstraintType.rightMarginSize)))
                .apply { onChange = { margin = it } }
        set(value) {
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.topMarginSize), value.top)
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.leftMarginSize), value.left)
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.bottomMarginSize), value.bottom)
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.rightMarginSize), value.right)
        }

    var horizontalContentHuggingPriority: ConstraintPriority
        get() = intrinsicSize.horizontalContentHuggingPriority
        set(value) {
            intrinsicSize.horizontalContentHuggingPriority = value
        }

    var verticalContentHuggingPriority: ConstraintPriority
        get() = intrinsicSize.verticalContentHuggingPriority
        set(value) {
            intrinsicSize.verticalContentHuggingPriority = value
        }

    var horizontalContentCompressionResistancePriority: ConstraintPriority
        get() = intrinsicSize.horizontalContentCompressionResistancePriority
        set(value) {
            intrinsicSize.horizontalContentCompressionResistancePriority = value
        }

    var verticalContentCompressionResistancePriority: ConstraintPriority
        get() = intrinsicSize.verticalContentCompressionResistancePriority
        set(value) {
            intrinsicSize.verticalContentCompressionResistancePriority = value
        }

    internal var intrinsicWidth: Double
        get() = intrinsicSize.intrinsicWidth
        set(value) {
            intrinsicSize.intrinsicWidth = value
        }

    internal var intrinsicHeight: Double
        get() = intrinsicSize.intrinsicHeight
        set(value) {
            intrinsicSize.intrinsicHeight = value
        }

    internal val constraintManager: ConstraintManager =
            (view as? AutoLayout ?: view.parent as? AutoLayout)?.constraintManager ?: throw AutoLayoutNotFoundException(view)

    private val intrinsicSize: IntrinsicSize by lazy {
        constraintManager.getViewIntrinsicSize(view)
    }

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
        constraintManager.removeViewConstraintsCreatedByUser(view)
        makeConstraints(closure)
    }

    fun debugValues() {
        val otherValues = "margin = $margin\n" +
                "intrinsicWidth = $intrinsicWidth\n" +
                "intrinsicHeight = $intrinsicHeight\n" +
                "horizontalContentHuggingPriority = $horizontalContentHuggingPriority\n" +
                "verticalContentHuggingPriority = $verticalContentHuggingPriority\n" +
                "horizontalContentCompressionResistancePriority = $horizontalContentCompressionResistancePriority\n" +
                "verticalContentCompressionResistancePriority = $verticalContentCompressionResistancePriority"
        (listOf(top, left, bottom, right, width, height, centerX, centerY, topMargin, leftMargin, bottomMargin, rightMargin,
                centerXWithMargins, centerYWithMargins)
                .map {
                    val value = constraintManager.getValueForVariable(it)
                    "${it.type} = ${if (value == 0.0) Math.abs(value) else value}"
                }
                .joinToString("\n") + "\n$otherValues")
                .let { Log.d("debugValues(view=${view.description})", it) }
    }

    fun debugValuesRecursive() {
        debugValues()
        if (view is AutoLayout) {
            view.children.forEach { it.snp.debugValuesRecursive() }
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
            view.children.forEach { it.snp.debugConstraintsRecursive() }
        }
    }

    private fun ConstraintVariable(type: ConstraintType): ConstraintVariable = ConstraintVariable(view, type)
}
