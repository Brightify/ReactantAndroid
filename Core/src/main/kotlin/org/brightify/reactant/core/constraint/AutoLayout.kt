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
        if (id == View.NO_ID) {
            id = View.generateViewId()
        }

        constraintSolver.addManagedView(id)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (!changed) {
            return
        }

        (0 until childCount)
                .map { getChildAt(it) }
                .forEach {
                    it.layout(
                            dpiForVariable(it.snp.left),
                            dpiForVariable(it.snp.top),
                            dpiForVariable(it.snp.right),
                            dpiForVariable(it.snp.bottom)
                    )
                }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun shouldDelayChildPressedState() = false

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        super.addView(child, index, params)

        if (child?.id == View.NO_ID) {
            child.id = View.generateViewId()
        }

        child?.id?.let { constraintSolver.addManagedView(it) }
    }

    fun children(vararg children: View): AutoLayout {
        children.forEach(this::addView)
        return this
    }

    private fun dpiForVariable(variable: ConstraintVariable): Int {
        return (constraintSolver.valueForVariable(variable) * resources.displayMetrics.density).toInt()
    }
}