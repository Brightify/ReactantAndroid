package org.brightify.reactant.core.constraint

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.brightify.reactant.core.assignId
import org.brightify.reactant.core.constraint.dsl.ConstraintDsl
import org.brightify.reactant.core.constraint.internal.manager.ConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.ContainerConstraintManager
import org.brightify.reactant.core.constraint.internal.manager.MainConstraintManager
import org.brightify.reactant.core.constraint.util.snp

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ContainerView : ViewGroup {

    @Suppress("LeakingThis")
    private val dsl = ConstraintDsl(this)

    internal var constraintManager: ConstraintManager = ContainerConstraintManager()

    private val density: Double
        get() = resources.displayMetrics.density.toDouble()

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr,
            defStyleRes)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
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
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        (0 until childCount).forEach {
            val child = getChildAt(it)
            val itDsl = child.snp
            child.measure(MeasureSpec.makeMeasureSpec(getValueForVariableInPx(itDsl.width), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getValueForVariableInPx(itDsl.height), MeasureSpec.EXACTLY))
        }
    }

    override fun shouldDelayChildPressedState() = false

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        child?.let {
            it.assignId()

            if (it is AutoLayout) {
                throw RuntimeException("")
            }
            (constraintManager as? MainConstraintManager)?.let { constraintManager ->
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
            (constraintManager as? MainConstraintManager)?.let { constraintManager ->
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
    }

    internal fun autoLayoutMeasure() {
        (0 until childCount).forEach {
            val child = getChildAt(it)
            if (child is ContainerView) {
                child.autoLayoutMeasure()
            } else if (child !is ViewGroup || child.layoutParams.width != LayoutParams.MATCH_PARENT || child.layoutParams.height != LayoutParams.MATCH_PARENT) {
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

                val itDsl = child.snp
                itDsl.intrinsicWidth = child.measuredWidth / density
                itDsl.intrinsicHeight = child.measuredHeight / density
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
}
