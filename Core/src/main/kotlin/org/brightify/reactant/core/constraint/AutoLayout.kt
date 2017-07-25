package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.assignId
import org.brightify.reactant.core.constraint.internal.AutoLayoutConstraints
import org.brightify.reactant.core.constraint.internal.ConstraintManager
import org.brightify.reactant.core.constraint.util.description
import org.brightify.reactant.core.constraint.util.forEachChildren
import org.brightify.reactant.core.constraint.util.snp
import org.brightify.reactant.core.util.printTimes

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class AutoLayout : ViewGroup {

    internal var constraintManager = ConstraintManager()
        private set

    private lateinit var autoLayoutConstraints: AutoLayoutConstraints

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
        autoLayoutConstraints = AutoLayoutConstraints(this)
        autoLayoutConstraints.isActive = true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val offsetTop = constraintManager.getValueForVariable(snp.top)
        val offsetLeft = constraintManager.getValueForVariable(snp.left)

        forEachChildren {
            it.layout(
                    getChildPosition(it.snp.left, offsetLeft),
                    getChildPosition(it.snp.top, offsetTop),
                    getChildPosition(it.snp.right, offsetLeft),
                    getChildPosition(it.snp.bottom, offsetTop)
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val begin = System.currentTimeMillis()

        measureIntrinsicSizes()

        val childrenMeasured = System.currentTimeMillis()

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED || MeasureSpec.getMode(
                heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            constraintManager.solve()
        }

        val firstSolve = System.currentTimeMillis()

        val measuredWidth = getMeasuredSize(widthMeasureSpec, snp.width)
        val measuredHeight = getMeasuredSize(heightMeasureSpec, snp.height)

        autoLayoutConstraints.width = measuredWidth
        autoLayoutConstraints.height = measuredHeight

        setMeasuredDimension((measuredWidth * density).toInt(), (measuredHeight * density).toInt())

        val measuredDimensionSet = System.currentTimeMillis()

        constraintManager.solve()

        val secondSolve = System.currentTimeMillis()

        measureRealSizes()

        val afterMeasure = System.currentTimeMillis()

        fun toSeconds(from: Long, to: Long): Double = (to - from).toDouble() / 1000F
        val timings = arrayListOf<Double>(
                toSeconds(begin, childrenMeasured),
                toSeconds(childrenMeasured, firstSolve),
                toSeconds(firstSolve, measuredDimensionSet),
                toSeconds(measuredDimensionSet, secondSolve),
                toSeconds(secondSolve, afterMeasure)
        )

        val wMode = MeasureSpec.getMode(widthMeasureSpec) shr 30
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec) shr 30
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        Log.d("ContainerView.onMeasure", "($description) Took ${toSeconds(begin,
                System.currentTimeMillis())}s totally where width: $wMode / $wSize and height: $hMode / $hSize. Timings: $timings\n\n")
        printTimes()
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            it.assignId()

            if (child is AutoLayout) {
                constraintManager.join(child.constraintManager)
                setConstraintManagerRecursive(child, constraintManager)
                child.autoLayoutConstraints.isActive = false
            } else {
                constraintManager.addManagedView(child)
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        child?.let {
            if (child is AutoLayout) {
                setConstraintManagerRecursive(child, constraintManager.split(child))
                child.autoLayoutConstraints.isActive = true
            } else {
                constraintManager.removeManagedView(child)
            }
        }
    }

    private fun measureIntrinsicSizes() {
        forEachChildren {
            if (it is AutoLayout) {
                it.measureIntrinsicSizes()
            } else if (constraintManager.getIntrinsicSizeManager(it) != null) {
                val needsWidth = constraintManager.needsIntrinsicWidth(it)
                val needsHeight = constraintManager.needsIntrinsicHeight(it)
                if (needsWidth || needsHeight) {
                    it.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                }

                it.snp.intrinsicWidth = if (needsWidth) it.measuredWidth / density else -1.0
                it.snp.intrinsicHeight = if (needsHeight) it.measuredHeight / density else -1.0
            }
        }
    }

    private fun measureRealSizes() {
        forEachChildren {
            if (it is AutoLayout) {
                it.measureRealSizes()
            } else {
                it.measure(MeasureSpec.makeMeasureSpec(getValueForVariableInPx(it.snp.width), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getValueForVariableInPx(it.snp.height), MeasureSpec.EXACTLY))
            }
        }
    }

    private fun getChildPosition(variable: ConstraintVariable, offset: Double): Int {
        val positionInDpi = constraintManager.getValueForVariable(variable) - offset
        return (positionInDpi * density).toInt()
    }

    private fun getMeasuredSize(measureSpec: Int, currentSize: ConstraintVariable): Double {
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> MeasureSpec.getSize(measureSpec) / density
            else -> constraintManager.getValueForVariable(currentSize)
        }
    }

    private fun getValueForVariableInPx(variable: ConstraintVariable): Int {
        return (constraintManager.getValueForVariable(variable) * density).toInt()
    }

    private fun setConstraintManagerRecursive(view: AutoLayout, constraintManager: ConstraintManager) {
        view.constraintManager = constraintManager
        view.forEachChildren {
            if (it is AutoLayout) {
                setConstraintManagerRecursive(it, constraintManager)
            }
        }
    }
}
