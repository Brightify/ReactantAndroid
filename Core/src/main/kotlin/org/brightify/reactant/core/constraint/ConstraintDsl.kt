package org.brightify.reactant.core.constraint

import android.util.Log
import android.view.View
import org.brightify.reactant.core.constraint.exception.AutoLayoutNotFoundException

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ConstraintDsl internal constructor(private val view: View) {

    internal val constraintManager: ConstraintManager by lazy {
        findConstraintSolver(view)
    }

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
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.topMarginSize)),
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.leftMarginSize)),
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.bottomMarginSize)),
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.rightMarginSize)))
                .apply { onChange = { margin = it } }
        set(value) {
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.topMarginSize), value.top)
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.leftMarginSize), value.left)
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.bottomMarginSize), value.bottom)
            constraintManager.setValueForVariable(ConstraintVariable(ConstraintType.rightMarginSize), value.right)
        }

    fun makeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        val createdConstraints = ArrayList<Constraint>()
        closure(ConstraintMakerProvider(view, createdConstraints))
        createdConstraints.filter { it.isActive }.forEach { it.activate() }
    }

    fun remakeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        constraintManager.removeConstraintsCreatedFromView(view)
        makeConstraints(closure)
    }

    fun debugValues() {
        (listOf(top, left, bottom, right, width, height, centerX, centerY, topMargin, leftMargin, bottomMargin, rightMargin,
                centerXWithMargins, centerYWithMargins, ConstraintVariable(ConstraintType.intrinsicWidth),
                ConstraintVariable(ConstraintType.intrinsicHeight))
                .map {
                    val value = constraintManager.valueForVariable(it)
                    "${it.type} = ${if (value == 0.0) Math.abs(value) else value}"
                }
                .joinToString("\n") + "\nmargin = $margin")
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

    private tailrec fun findConstraintSolver(view: View): ConstraintManager {
        if (view is AutoLayout) {
            return view.constraintManager
        } else {
            return findConstraintSolver(view.parent as? View ?: throw AutoLayoutNotFoundException(this.view))
        }
    }
}
