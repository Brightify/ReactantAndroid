package org.brightify.reactant.autolayout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.autolayout.internal.AutoLayoutConstraints
import org.brightify.reactant.autolayout.internal.ConstraintManager
import org.brightify.reactant.autolayout.internal.util.isAlmostZero
import org.brightify.reactant.autolayout.util.description
import org.brightify.reactant.autolayout.util.forEachChild
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.core.util.assignId
import org.brightify.reactant.core.util.onChange
import kotlin.math.min
import kotlin.math.roundToInt

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class AutoLayout: ViewGroup {

    var measureTime: Boolean by onChange(false) { _, _, _ ->
        (parent as? AutoLayout)?.measureTime = measureTime
        forEachChild {
            if (it is AutoLayout) {
                it.measureTime = measureTime
            }
        }
    }

    internal var constraintManager = ConstraintManager()
        private set

    private val autoLayoutConstraints: AutoLayoutConstraints
        get() = constraintManager.viewConstraints[this]?.autoLayoutConstraints ?: throw IllegalStateException(
                "Missing AutoLayoutConstraints.")

    private val density: Double
        get() = resources.displayMetrics.density.toDouble()

    constructor(context: Context?): super(context)

    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr,
            defStyleRes)

    init {
        assignId()

        constraintManager.addManagedView(this)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val offsetTop = constraintManager.getValueForVariable(snp.top)
        val offsetLeft = constraintManager.getValueForVariable(snp.left)

        forEachChild {
            it.layout(
                    getChildPosition(it.snp.left, offsetLeft),
                    getChildPosition(it.snp.top, offsetTop),
                    getChildPosition(it.snp.right, offsetLeft),
                    getChildPosition(it.snp.bottom, offsetTop)
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (parent is AutoLayout) {
            setMeasuredSize(widthMeasureSpec, heightMeasureSpec)
            measureRealSizes()
        } else {
            val begin = System.nanoTime()

            initializeAutoLayoutConstraints(widthMeasureSpec, heightMeasureSpec)
            constraintManager.updateIntrinsicSizeNecessityDecider()
            updateVisibility()
            requestLayoutRecursive(this)
            measureIntrinsicSizes()
            setMeasuredSize(widthMeasureSpec, heightMeasureSpec)
            measureIntrinsicHeights()
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY || MeasureSpec.getMode(
                            heightMeasureSpec) != MeasureSpec.EXACTLY) {
                setMeasuredSize(widthMeasureSpec, heightMeasureSpec)
            }
            measureRealSizes()

            if (measureTime) {
                val time = (System.nanoTime() - begin) / 1e9

                val wMode = MeasureSpec.getMode(widthMeasureSpec) shr 30
                val wSize = MeasureSpec.getSize(widthMeasureSpec)
                val hMode = MeasureSpec.getMode(heightMeasureSpec) shr 30
                val hSize = MeasureSpec.getSize(heightMeasureSpec)

                Log.d("AutoLayout.onMeasure",
                        "($description) Took $time s totally where width: $wMode / $wSize and height: $hMode / $hSize " +
                                "constraint count: ${constraintManager.allConstraints.count()}.\n\n")
            }
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
                child.autoLayoutConstraints.setActive(false)
                measureTime = measureTime or child.measureTime
                child.measureTime = measureTime
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
                child.autoLayoutConstraints.setActive(true)
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
            autoLayoutConstraints.width = MeasureSpec.getSize(widthMeasureSpec).toDp()
        }
        autoLayoutConstraints.heightIsAtMost = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            autoLayoutConstraints.height = -1.0
        } else {
            autoLayoutConstraints.height = MeasureSpec.getSize(heightMeasureSpec).toDp()
        }
    }

    private fun updateVisibility() {
        constraintManager.getVisibilityManager(this).visibility = visibility
        forEachChild {
            if (it is AutoLayout) {
                it.updateVisibility()
            } else {
                constraintManager.getVisibilityManager(it).visibility = it.visibility
            }
        }
    }

    private fun measureIntrinsicSizes() {
        forEachChild {
            if (it is AutoLayout) {
                it.measureIntrinsicSizes()
            } else if (constraintManager.getIntrinsicSizeManager(it) != null) {
                val needsWidth = constraintManager.needsIntrinsicWidth(it)
                val needsHeight = constraintManager.needsIntrinsicHeight(it)
                if (needsWidth || needsHeight) {
                    it.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                }

                it.snp.intrinsicWidth = if (needsWidth) it.measuredWidth.toDp() else -1.0
                it.snp.intrinsicHeight = if (needsHeight) it.measuredHeight.toDp() else -1.0
            }
        }
    }

    private fun measureIntrinsicHeights() {
        forEachChild {
            if (it is AutoLayout) {
                it.measureIntrinsicHeights()
            } else if (constraintManager.needsIntrinsicHeight(it)
                    && !(constraintManager.getValueForVariable(it.snp.width) - it.snp.intrinsicWidth).isAlmostZero) {
                it.measure(MeasureSpec.makeMeasureSpec(constraintManager.getValueForVariable(it.snp.width).toPx(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                val measuredHeight = it.measuredHeight.toDp()
                if (!(it.snp.intrinsicHeight - measuredHeight).isAlmostZero) {
                    it.snp.intrinsicHeight = measuredHeight
                }
            }
        }
    }

    private fun measureRealSizes() {
        forEachChild {
            it.measure(MeasureSpec.makeMeasureSpec(constraintManager.getValueForVariable(it.snp.width).toPx(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(constraintManager.getValueForVariable(it.snp.height).toPx(), MeasureSpec.EXACTLY))
        }
    }

    private fun getChildPosition(variable: ConstraintVariable, offset: Double): Int = (constraintManager.getValueForVariable(
            variable) - offset).toPx()

    private fun setMeasuredSize(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        fun getMeasuredSize(measureSpec: Int, currentSize: ConstraintVariable): Int {
            return when (MeasureSpec.getMode(measureSpec)) {
                MeasureSpec.EXACTLY -> MeasureSpec.getSize(measureSpec)
                MeasureSpec.UNSPECIFIED -> constraintManager.getValueForVariable(currentSize).toPx()
                else -> min(MeasureSpec.getSize(measureSpec), constraintManager.getValueForVariable(currentSize).toPx())
            }
        }

        setMeasuredDimension(getMeasuredSize(widthMeasureSpec, snp.width), getMeasuredSize(heightMeasureSpec, snp.height))
    }

    private fun setConstraintManagerRecursive(view: AutoLayout, constraintManager: ConstraintManager) {
        view.constraintManager = constraintManager
        view.forEachChild {
            if (it is AutoLayout) {
                setConstraintManagerRecursive(it, constraintManager)
            }
        }
    }

    private fun requestLayoutRecursive(view: View) {
        view.requestLayout()
        (view as? AutoLayout)?.forEachChild {
            requestLayoutRecursive(it)
        }
    }

    private fun Double.toPx(): Int = (this * density).roundToInt()

    private fun Int.toDp(): Double = this / density
}
