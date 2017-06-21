package org.brightify.reactant.core.constraint

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.constraint.internal.ConstraintManager
import org.brightify.reactant.core.constraint.internal.ConstraintType
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class AutoLayout : ViewGroup {

    internal var constraintManager = ConstraintManager()

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
        constraintManager.setValueForVariable(snp.top, 0)
        constraintManager.setValueForVariable(snp.left, 0)

        if (id == View.NO_ID) {
            id = View.generateViewId()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        (0 until childCount).map { getChildAt(it) }.forEach {
            val dsl = it.snp
            it.layout(
                    getChildPosition(dsl.left),
                    getChildPosition(dsl.top),
                    getChildPosition(dsl.right),
                    getChildPosition(dsl.bottom)
            )
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dsl = snp
        constraintManager.resetValueForVariable(dsl.width)
        constraintManager.resetValueForVariable(dsl.height)

        (0 until childCount).map { getChildAt(it) }.forEach {
            it.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

            if (it !is AutoLayout) {
                constraintManager.setValueForVariable(ConstraintVariable(it, ConstraintType.intrinsicWidth), it.measuredWidth / density)
                constraintManager.setValueForVariable(ConstraintVariable(it, ConstraintType.intrinsicHeight), it.measuredHeight / density)
            }
        }

        updateSizeConstraint(widthMeasureSpec, dsl.width)
        updateSizeConstraint(heightMeasureSpec, dsl.height)

        setMeasuredDimension((constraintManager.valueForVariable(dsl.width) * density).toInt(),
                (constraintManager.valueForVariable(dsl.height) * density).toInt())
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintManager.delegateTo(constraintManager)
                it.constraintManager.resetValueForVariable(it.snp.top)
                it.constraintManager.resetValueForVariable(it.snp.left)
            } else {
                constraintManager.addManagedView(it)
            }

            if (it.id == View.NO_ID) {
                it.id = View.generateViewId()
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        child?.let {
            if (it is AutoLayout) {
                it.constraintManager.stopDelegation()
                it.constraintManager.setValueForVariable(snp.top, 0)
                it.constraintManager.setValueForVariable(snp.left, 0)
            } else {
                constraintManager.removeManagedView(it)
            }
        }
    }

    fun children(vararg children: View): AutoLayout {
        children.forEach(this::addView)
        return this
    }

    private fun getChildPosition(variable: ConstraintVariable): Int {
        val offsetType = when (variable.type) {
            ConstraintType.left, ConstraintType.right -> ConstraintType.left
            else -> ConstraintType.top
        }
        val offset = constraintManager.valueForVariable(ConstraintVariable(this, offsetType))
        val positionInDpi = constraintManager.valueForVariable(variable) - offset
        return (positionInDpi * density).toInt()
    }

    private fun updateSizeConstraint(measureSpec: Int, variable: ConstraintVariable) {
        when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> constraintManager.setValueForVariable(variable, MeasureSpec.getSize(measureSpec) / density)
            MeasureSpec.AT_MOST -> {
                val width = constraintManager.valueForVariable(variable)
                val maxWidth = MeasureSpec.getSize(measureSpec) / density
                if (width > maxWidth) {
                    constraintManager.setValueForVariable(variable, maxWidth)
                }
            }
        }
    }
}
