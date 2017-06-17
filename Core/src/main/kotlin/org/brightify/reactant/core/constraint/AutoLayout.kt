package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class AutoLayout : ViewGroup {

    internal var constraintSolver = ConstraintManager()

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr,
            defStyleRes) {
        init()
    }

    private fun init() {
        constraintSolver.addManagedView(this)
        constraintSolver.addConstraint(
                Constraint(this, listOf(
                        ConstraintItem(ConstraintVariable(this, ConstraintType.left), ConstraintOperator.equal),
                        ConstraintItem(ConstraintVariable(this, ConstraintType.top), ConstraintOperator.equal)
                ))
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        (0 until childCount).map { getChildAt(it) }.forEach {
            it.layout(
                    dpiForVariable(it.snp.left),
                    dpiForVariable(it.snp.top),
                    dpiForVariable(it.snp.right),
                    dpiForVariable(it.snp.bottom)
            )
        }
        snp.debugValuesRecursive()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // TODO

        (0 until childCount).map { getChildAt(it) }.forEach { it.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED) }

        if (!constraintSolver.isDelegated) {
            constraintSolver.updateIntrinsicSizes(resources.displayMetrics.density)
        }
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintSolver.delegateTo(constraintSolver)
            } else {
                constraintSolver.addManagedView(it)
            }
            it.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintSolver.stopDelegation()
            } else {
                constraintSolver.removeManagedView(it)
            }
        }
    }

    fun children(vararg children: View): AutoLayout {
        children.forEach(this::addView)
        return this
    }

    private fun dpiForVariable(variable: ConstraintVariable): Int {
        return (constraintSolver.valueForVariable(variable) * resources.displayMetrics.density).toInt()
    }
}