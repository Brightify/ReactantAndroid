package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.assignId
import org.brightify.reactant.core.constraint.internal.ConstraintType
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
        constraintManager.addManagedView(this)
        assignId()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        children.forEach {
            val dsl = it.snp
            it.layout(
                    getChildPosition(dsl.left),
                    getChildPosition(dsl.top),
                    getChildPosition(dsl.right),
                    getChildPosition(dsl.bottom)
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dsl = snp
        constraintManager.resetValueForVariable(dsl.top)
        constraintManager.resetValueForVariable(dsl.left)

        children.forEach {
            it.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

            if (it !is AutoLayout) {
                val itDsl = it.snp
                itDsl.intrinsicWidth = it.measuredWidth / density
                itDsl.intrinsicHeight = it.measuredHeight / density
            }
        }

        if (constraintManager is MainConstraintManager) {
            constraintManager.setValueForVariable(dsl.top, 0)
            constraintManager.setValueForVariable(dsl.left, 0)
        }

        dsl.intrinsicWidth = getMeasuredSize(widthMeasureSpec, constraintManager.getValueForVariable(dsl.width))
        dsl.intrinsicHeight = getMeasuredSize(heightMeasureSpec, constraintManager.getValueForVariable(dsl.height))

        setMeasuredDimension(getValueForVariableInPx(dsl.width), getValueForVariableInPx(dsl.height))

        if (constraintManager is MainConstraintManager) {
            afterMeasure()
        }
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintManager.addAllToManager(constraintManager)
                it.constraintManager = DelegatedConstraintManager(constraintManager)
            } else {
                constraintManager.addManagedView(it)
            }

            it.assignId()
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintManager = constraintManager.splitToMainManagerForAutoLayout(it)
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

    private fun getChildPosition(variable: ConstraintVariable): Int {
        val offsetType = when (variable.type) {
            ConstraintType.left, ConstraintType.right -> ConstraintType.left
            else -> ConstraintType.top
        }
        val offset = constraintManager.getValueForVariable(ConstraintVariable(this, offsetType))
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
            else -> 0.0
        }
    }
}
