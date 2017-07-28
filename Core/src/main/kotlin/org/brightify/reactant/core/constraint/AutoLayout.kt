package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.assignId
import org.brightify.reactant.core.constraint.internal.AutoLayoutConstraints
import org.brightify.reactant.core.constraint.internal.ConstraintManager
import org.brightify.reactant.core.constraint.internal.util.isAlmostZero
import org.brightify.reactant.core.constraint.util.description
import org.brightify.reactant.core.constraint.util.forEachChildren
import org.brightify.reactant.core.constraint.util.snp
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class AutoLayout : ViewGroup {

    var measureTime: Boolean by Delegates.observable(false) { _, _, _ ->
        (parent as? AutoLayout)?.measureTime = measureTime
    }

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
        val begin = System.nanoTime()

        initializeAutoLayoutConstraints(widthMeasureSpec, heightMeasureSpec)
        resetVisibility()
        measureIntrinsicSizes()
        setMeasuredSize(widthMeasureSpec, heightMeasureSpec)
        constraintManager.solve()
        measureIntrinsicHeights()
        updateVisibility()
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            setMeasuredSize(widthMeasureSpec, heightMeasureSpec)
        } else {
            constraintManager.solve()
        }
        measureRealSizes()

        if (measureTime) {
            val time = (System.nanoTime() - begin) / 1e9

            val wMode = MeasureSpec.getMode(widthMeasureSpec) shr 30
            val wSize = MeasureSpec.getSize(widthMeasureSpec)
            val hMode = MeasureSpec.getMode(heightMeasureSpec) shr 30
            val hSize = MeasureSpec.getSize(heightMeasureSpec)

            Log.d("AutoLayout.onMeasure",
                    "($description) Took $time s totally where width: $wMode / $wSize and height: $hMode / $hSize.\n\n")
        }
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
                measureTime = measureTime or child.measureTime
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

    private fun initializeAutoLayoutConstraints(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        autoLayoutConstraints.widthIsAtMost = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            autoLayoutConstraints.width = -1.0
        } else {
            autoLayoutConstraints.width = MeasureSpec.getSize(widthMeasureSpec) / density
        }
        autoLayoutConstraints.heightIsAtMost = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            autoLayoutConstraints.height = -1.0
        } else {
            autoLayoutConstraints.height = MeasureSpec.getSize(heightMeasureSpec) / density
        }
    }

    private fun resetVisibility() {
        constraintManager.getVisibilityManager(this).visibility = View.VISIBLE
        forEachChildren {
            constraintManager.getVisibilityManager(it).visibility = View.VISIBLE
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

    private fun measureIntrinsicHeights() {
        forEachChildren {
            if (it is AutoLayout) {
                it.measureIntrinsicHeights()
            } else if (constraintManager.needsIntrinsicHeight(it)
                    && constraintManager.getIntrinsicSizeManager(it) != null
                    && !(constraintManager.getValueForVariable(it.snp.width) - it.snp.intrinsicWidth).isAlmostZero) {
                it.measure(MeasureSpec.makeMeasureSpec(getValueForVariableInPx(it.snp.width), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                val measuredHeight = it.measuredHeight / density
                if (!(it.snp.intrinsicHeight - measuredHeight).isAlmostZero) {
                    it.snp.intrinsicHeight = measuredHeight
                }
            }
        }
    }

    private fun updateVisibility() {
        constraintManager.getVisibilityManager(this).visibility = visibility
        forEachChildren {
            constraintManager.getVisibilityManager(it).visibility = it.visibility
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

    private fun setMeasuredSize(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        fun getMeasuredSize(measureSpec: Int, currentSize: ConstraintVariable): Int {
            return when (MeasureSpec.getMode(measureSpec)) {
                MeasureSpec.EXACTLY -> MeasureSpec.getSize(measureSpec)
                MeasureSpec.UNSPECIFIED -> getValueForVariableInPx(currentSize)
                else -> Math.min(MeasureSpec.getSize(measureSpec), getValueForVariableInPx(currentSize))
            }
        }

        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            constraintManager.solve()
        }

        setMeasuredDimension(getMeasuredSize(widthMeasureSpec, snp.width), getMeasuredSize(heightMeasureSpec, snp.height))
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
