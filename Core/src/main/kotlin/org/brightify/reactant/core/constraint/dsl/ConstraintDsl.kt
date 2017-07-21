package org.brightify.reactant.core.constraint.dsl

import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.Constraint
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.ConstraintVariable
import org.brightify.reactant.core.constraint.ContainerView
import org.brightify.reactant.core.constraint.exception.AutoLayoutNotFoundException
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.internal.ViewEquationsManagerWithIntrinsicSize
import org.brightify.reactant.core.constraint.internal.manager.ConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.MainConstraintManager
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

    var horizontalContentHuggingPriority: ConstraintPriority
        get() = viewEquationsManagerWithIntrinsicSize.horizontalContentHuggingPriority
        set(value) {
            viewEquationsManagerWithIntrinsicSize.horizontalContentHuggingPriority = value
        }

    var verticalContentHuggingPriority: ConstraintPriority
        get() = viewEquationsManagerWithIntrinsicSize.verticalContentHuggingPriority
        set(value) {
            viewEquationsManagerWithIntrinsicSize.verticalContentHuggingPriority = value
        }

    var horizontalContentCompressionResistancePriority: ConstraintPriority
        get() = viewEquationsManagerWithIntrinsicSize.horizontalContentCompressionResistancePriority
        set(value) {
            viewEquationsManagerWithIntrinsicSize.horizontalContentCompressionResistancePriority = value
        }

    var verticalContentCompressionResistancePriority: ConstraintPriority
        get() = viewEquationsManagerWithIntrinsicSize.verticalContentCompressionResistancePriority
        set(value) {
            viewEquationsManagerWithIntrinsicSize.verticalContentCompressionResistancePriority = value
        }

    internal var intrinsicWidth: Double
        get() = viewEquationsManagerWithIntrinsicSize.intrinsicWidth
        set(value) {
            viewEquationsManagerWithIntrinsicSize.intrinsicWidth = value
        }

    internal var intrinsicHeight: Double
        get() = viewEquationsManagerWithIntrinsicSize.intrinsicHeight
        set(value) {
            viewEquationsManagerWithIntrinsicSize.intrinsicHeight = value
        }

    // TODO
    private val constraintManager: ConstraintManager by lazy {
        val containerViewConstraintManager = (view.parent as? ContainerView ?: view as? ContainerView)?.constraintManager
        val autoLayoutConstraintManager = (view.parent as? AutoLayout ?: view as? AutoLayout)?.constraintManager
        containerViewConstraintManager ?: autoLayoutConstraintManager ?: throw AutoLayoutNotFoundException(view)
    }

    private val viewEquationsManagerWithIntrinsicSize: ViewEquationsManagerWithIntrinsicSize by lazy {
        constraintManager.getEquationsManager(view) as? ViewEquationsManagerWithIntrinsicSize ?: throw RuntimeException(
                "${view.description} does not have intrinsic size.")
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
        constraintManager.removeViewConstraints(view)
        makeConstraints(closure)
    }

    fun debugValues() {
        val otherValues = if ((constraintManager as MainConstraintManager).getEquationsManager(
                view) is ViewEquationsManagerWithIntrinsicSize) "\nintrinsicWidth = $intrinsicWidth\n" +
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
        if (view is ContainerView || view is AutoLayout) {
            (view as ViewGroup).children.forEach { it.snp.debugValuesRecursive() }
        }
    }

    fun debugConstraints() {
        (constraintManager as MainConstraintManager).allConstraints
                .flatMap { it.constraintItems }
                .filter { it.leftVariable.view == view || it.rightVariable?.view == view }
                .map { it.toString() }
                .joinToString("\n").let { Log.d("debugConstraints(view=${view.description})", it) }
    }

    fun debugConstraintsRecursive() {
        debugConstraints()
        if (view is ContainerView || view is AutoLayout) {
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
