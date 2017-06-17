package org.brightify.reactant.core.constraint

import android.util.Log
import android.view.View

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

    val leading: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.leading)

    val trailing: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.trailing)

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
        get() = ConstraintVariable(ConstraintType.leadingMargin)

    val trailingMargin: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.trailingMargin)

    val centerXWithMargins: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.centerXWithMargins)

    val centerYWithMargins: ConstraintVariable
        get() = ConstraintVariable(ConstraintType.centerYWithMargins)

    var margin: Margin
        get() = MarginModifier(
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.topMarginSize)),
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.leftMarginSize)),
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.bottomMarginSize)),
                constraintManager.valueForVariable(ConstraintVariable(ConstraintType.rightMarginSize)),
                { margin = it }
        )
        set(value) {
            constraintManager.getValueConstraint(ConstraintVariable(ConstraintType.topMarginSize)).offset = value.top
            constraintManager.getValueConstraint(ConstraintVariable(ConstraintType.leftMarginSize)).offset = value.left
            constraintManager.getValueConstraint(ConstraintVariable(ConstraintType.bottomMarginSize)).offset = value.bottom
            constraintManager.getValueConstraint(ConstraintVariable(ConstraintType.rightMarginSize)).offset = value.right
        }

    fun makeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        val createdConstraints = ArrayList<Constraint>()
        closure(ConstraintMakerProvider(view, createdConstraints))
        createdConstraints.filter { it.isActive }.forEach { it.activate() }
    }

    fun remakeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        constraintManager.removeConstraintCreatedFromView(view)
        makeConstraints(closure)
    }

    fun debugValues(description: String = view.javaClass.simpleName) {
        listOf(top, left, bottom, right, width, height, centerX, centerY, topMargin, leftMargin, bottomMargin, rightMargin,
                centerXWithMargins, centerYWithMargins).forEach {
            Log.d("debugValues(view=$description)",
                    it.type.toString().padEnd(18) + " = " + constraintManager.valueForVariable(it).toString()
            )
        }
        Log.d("debugValues(view=$description)", "margin".padEnd(18) + " = " + margin.toString())
    }

    fun debugValuesRecursive() {
        if (view is AutoLayout) {
            debugValues()
            (0 until view.childCount).map { ConstraintDsl(view.getChildAt(it)) }.forEach { it.debugValuesRecursive() }
        } else {
            debugValues()
        }
    }

    fun debugConstraints(recursive: Boolean = false) {
        if (recursive && view is AutoLayout) {
            debugConstraints()
            (0 until view.childCount).map { ConstraintDsl(view.getChildAt(it)) }.forEach { it.debugConstraints(true) }
        } else {
            // TODO
        }
    }

    private fun ConstraintVariable(type: ConstraintType): ConstraintVariable = ConstraintVariable(view, type)

    private tailrec fun findConstraintSolver(view: View): ConstraintManager {
        if (view is AutoLayout) {
            return view.constraintSolver
        } else {
            return findConstraintSolver(view.parent as? View ?: throw RuntimeException()) // TODO
        }
    }
}
