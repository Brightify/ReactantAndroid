package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.assignId
import org.brightify.reactant.core.constraint.dsl.ConstraintDsl
import org.brightify.reactant.core.constraint.internal.manager.ConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.ContainerConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.MainConstraintManager
import org.brightify.reactant.core.constraint.util.description
import org.brightify.reactant.core.constraint.util.snp
import org.brightify.reactant.core.util.printTimes

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class AutoLayout : ViewGroup {

    internal var constraintManager = MainConstraintManager()
        private set

    private var lastWidth = -1
    private var lastHeight = -1
    private var lastMeasuredWidth = -1
    private var lastMeasuredHeight = -1

    private val dsl = ConstraintDsl(this)

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

        constraintManager.setValueForVariable(dsl.top, 0)
        constraintManager.setValueForVariable(dsl.left, 0)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        constraintManager.solve()

        val offsetTop = constraintManager.getValueForVariable(dsl.top)
        val offsetLeft = constraintManager.getValueForVariable(dsl.left)

        (0 until childCount).forEach {
            val child = getChildAt(it)
            val itDsl = child.snp
            child.layout(
                    getChildPosition(itDsl.left, offsetLeft),
                    getChildPosition(itDsl.top, offsetTop),
                    getChildPosition(itDsl.right, offsetLeft),
                    getChildPosition(itDsl.bottom, offsetTop)
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val currentMeasuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val currentMeasuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (lastMeasuredWidth == currentMeasuredWidth && lastMeasuredHeight == currentMeasuredHeight) {
            setMeasuredDimension(lastWidth, lastHeight)
            return
        } else {
            lastMeasuredWidth = currentMeasuredWidth
            lastMeasuredHeight = currentMeasuredHeight
        }

        val begin = System.currentTimeMillis()

        (0 until childCount).forEach {
            val child = getChildAt(it)
            if (child is ContainerView) {
                child.autoLayoutMeasure()
            } else {
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

                val itDsl = child.snp
                itDsl.intrinsicWidth = child.measuredWidth / density
                itDsl.intrinsicHeight = child.measuredHeight / density
            }
        }

        val childrenMeasured = System.currentTimeMillis()

        constraintManager.solve()

        val firstSolve = System.currentTimeMillis()

        val measuredWidth = getMeasuredSize(widthMeasureSpec, constraintManager.getValueForVariable(dsl.width))
        val measuredHeight = getMeasuredSize(heightMeasureSpec, constraintManager.getValueForVariable(dsl.height))

        constraintManager.setValueForVariable(dsl.width, measuredWidth)
        constraintManager.setValueForVariable(dsl.height, measuredHeight)

        lastWidth = (measuredWidth * density).toInt()
        lastHeight = (measuredHeight * density).toInt()
        setMeasuredDimension(lastWidth, lastHeight)

        val measuredDimensionSet = System.currentTimeMillis()

        constraintManager.solve()

        val secondSolve = System.currentTimeMillis()

        (0 until childCount).forEach {
            val child = getChildAt(it)
            val itDsl = child.snp
            child.measure(MeasureSpec.makeMeasureSpec(getValueForVariableInPx(itDsl.width), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getValueForVariableInPx(itDsl.height), MeasureSpec.EXACTLY))
        }

        val afterMeasure = System.currentTimeMillis()

        fun toSeconds(from: Long, to: Long): Double = (to - from).toDouble() / 1000F
        val timings = arrayListOf<Double>(
                toSeconds(begin, childrenMeasured),
                toSeconds(childrenMeasured, firstSolve),
                toSeconds(firstSolve, measuredDimensionSet),
                toSeconds(measuredDimensionSet, secondSolve),
                toSeconds(secondSolve, afterMeasure)
        )

        if (constraintManager is ConstraintManager) {
            val wMode = MeasureSpec.getMode(widthMeasureSpec) shr 30
            val wSize = MeasureSpec.getSize(widthMeasureSpec)
            val hMode = MeasureSpec.getMode(heightMeasureSpec) shr 30
            val hSize = MeasureSpec.getSize(heightMeasureSpec)

            Log.d("ContainerView.onMeasure", "($description) Took ${toSeconds(begin,
                    System.currentTimeMillis())}s totally where width: $wMode / $wSize and height: $hMode / $hSize. Timings: $timings\n\n")
            printTimes()
        }
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            it.assignId()

            if (it is AutoLayout) {
                throw RuntimeException("")
            } else {
                val managersToAdd = HashSet<ContainerConstraintManager>()
                fun addViewRecursive(view: View) {
                    constraintManager.addManagedView(view)
                    if (view is ContainerView) {
                        (view.constraintManager as? ContainerConstraintManager)?.let { managersToAdd.add(it) }
                        view.constraintManager = constraintManager
                        (0 until view.childCount).forEach {
                            addViewRecursive(view.getChildAt(it))
                        }
                    }
                }
                addViewRecursive(it)
                constraintManager.addAll(managersToAdd)
            }
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        child?.let {
            fun removeViewRecursive(view: View) {
                constraintManager.removeManagedView(view)
                if (view is ContainerView) {
                    view.constraintManager = ContainerConstraintManager()
                    (0 until view.childCount).forEach {
                        removeViewRecursive(view.getChildAt(it))
                    }
                }
            }

            removeViewRecursive(it)
        }
    }

    private fun getChildPosition(variable: ConstraintVariable, offset: Double): Int {
        val positionInDpi = constraintManager.getValueForVariable(variable) - offset
        return (positionInDpi * density).toInt()
    }

    private fun getMeasuredSize(measureSpec: Int, currentSize: Double): Double {
        return when (MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> MeasureSpec.getSize(measureSpec) / density
//            MeasureSpec.AT_MOST -> Math.min(currentSize, MeasureSpec.getSize(measureSpec) / density)
            else -> currentSize
        }
    }

    private fun getValueForVariableInPx(variable: ConstraintVariable): Int {
        return (constraintManager.getValueForVariable(variable) * density).toInt()
    }
}
