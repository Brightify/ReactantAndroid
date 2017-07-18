package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.assignId
import org.brightify.reactant.core.constraint.internal.manager.ConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.DelegatedConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.MainConstraintManager
import org.brightify.reactant.core.constraint.util.children
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class AutoLayout : ViewGroup {

    internal var constraintManager: ConstraintManager = MainConstraintManager()
        private set

    private val density: Double
        get() = resources.displayMetrics.density.toDouble()

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
        assignId()

        constraintManager.addManagedView(this)

        constraintManager.setValueForVariable(snp.top, 0)
        constraintManager.setValueForVariable(snp.left, 0)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val dsl = snp
        val offsetTop = constraintManager.getValueForVariable(dsl.top)
        val offsetLeft = constraintManager.getValueForVariable(dsl.left)

        children.forEach {
            val itDsl = it.snp
            it.layout(
                    getChildPosition(itDsl.left, offsetLeft),
                    getChildPosition(itDsl.top, offsetTop),
                    getChildPosition(itDsl.right, offsetLeft),
                    getChildPosition(itDsl.bottom, offsetTop)
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        children.forEach {
            it.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

            if (it !is AutoLayout) {
                val itDsl = it.snp
                itDsl.intrinsicWidth = it.measuredWidth / density
                itDsl.intrinsicHeight = it.measuredHeight / density
            }
        }

        constraintManager.mainConstraintManager.solver.solve()

        val dsl = snp
        if (constraintManager is MainConstraintManager) {
            constraintManager.setValueForVariable(dsl.width, getMeasuredSize(widthMeasureSpec, constraintManager.getValueForVariable(dsl.width)))
            constraintManager.setValueForVariable(dsl.height, getMeasuredSize(heightMeasureSpec, constraintManager.getValueForVariable(dsl.height)))
            constraintManager.mainConstraintManager.solver.solve()
        }

        setMeasuredDimension(getValueForVariableInPx(dsl.width), getValueForVariableInPx(dsl.height))

        if (constraintManager is MainConstraintManager) {
            afterMeasure()
        }
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            it.assignId()

            if (it is AutoLayout) {
                it.constraintManager.resetValueForVariable(it.snp.top)
                it.constraintManager.resetValueForVariable(it.snp.left)
                it.constraintManager.addAllToManager(constraintManager)
                it.constraintManager = DelegatedConstraintManager(this)
            } else {
                constraintManager.addManagedView(it)
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintManager = constraintManager.splitToMainManagerForAutoLayout(it)
                it.constraintManager.setValueForVariable(it.snp.top, 0)
                it.constraintManager.setValueForVariable(it.snp.left, 0)
            } else {
                constraintManager.removeManagedView(it)
            }
        }
    }

    private fun afterMeasure() {
        children.forEach {
            if (it is AutoLayout) {
                it.afterMeasure()
            } else {
                val dsl = it.snp
                it.measure(MeasureSpec.makeMeasureSpec(getValueForVariableInPx(dsl.width), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getValueForVariableInPx(dsl.height), MeasureSpec.EXACTLY))
            }
        }
    }

    private fun getChildPosition(variable: ConstraintVariable, offset: Double): Int {
        val positionInDpi = constraintManager.getValueForVariable(variable) - offset
        return (positionInDpi * density).toInt()
    }

    private fun getValueForVariableInPx(variable: ConstraintVariable): Int {
        return (constraintManager.getValueForVariable(variable) * density).toInt()
    }

    private fun getMeasuredSize(measureSpec: Int, currentSize: Double): Double {
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(measureSpec) / density
            MeasureSpec.AT_MOST -> Math.min(currentSize, MeasureSpec.getSize(measureSpec) / density)
            else -> currentSize
        }
    }
}
