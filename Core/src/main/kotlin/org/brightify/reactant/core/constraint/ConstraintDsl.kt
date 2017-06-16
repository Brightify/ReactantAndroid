package org.brightify.reactant.core.constraint

import android.util.Log
import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ConstraintDsl internal constructor(private val view: View) {

    internal val constraintManager: ConstraintManager
        get() = findConstraintSolver(view)

    val left: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.left)

    val top: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.top)

    val right: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.right)

    val bottom: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.bottom)

    val width: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.width)

    val height: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.height)

    val centerX: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.centerX)

    val centerY: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.centerY)

    val leading: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.leading)

    val trailing: ConstraintVariable
        get() = ConstrainVariable(ConstraintType.trailing)

    fun makeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
        val createdConstraints = ArrayList<Constraint>()
        closure(ConstraintMakerProvider(view, createdConstraints))
        createdConstraints.forEach { it.activate() }
    }

//    fun remakeConstraints(closure: ConstraintMakerProvider.() -> Unit) {
//        closure(ConstraintMakerProvider(view))
//    }
//
//    fun updateConstraints(closure: ConstraintMakerProvider.() -> Unit) {
//        closure(ConstraintMakerProvider(view))
//    }

    fun debugValues(recursive: Boolean = false) {
        if (recursive && view is AutoLayout) {
            debugValues()
            (0 until view.childCount).map { ConstraintDsl(view.getChildAt(it)) }.forEach { it.debugValues(true) }
        } else {
            listOf(top, left, bottom, right, width, height, centerX, centerY).forEach {
                Log.d("debugValues(type=${view.javaClass.simpleName}, viewId=${view.id})",
                        it.type.toString().padEnd(7) + " = " + constraintManager.valueForVariable(it).toString()
                )
            }
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

    private fun ConstrainVariable(type: ConstraintType): ConstraintVariable = ConstraintVariable(view.id, type)

    private tailrec fun findConstraintSolver(view: View): ConstraintManager {
        if (view is AutoLayout) {
            return view.constraintSolver
        } else {
            return findConstraintSolver(view.parent as? View ?: throw RuntimeException()) // TODO
        }
    }
}
