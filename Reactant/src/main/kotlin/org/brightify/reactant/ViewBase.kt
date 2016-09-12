package org.brightify.reactant

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Created by TadeasKriz on 12/09/16.
 */
abstract class ViewBase<STATE: Any>(stateType: Class<STATE>, context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
        View(context, attrs, defStyleAttr, defStyleRes), Component<STATE> by ComponentBase(stateType) {

    constructor(context: Context, initialState: STATE): this(initialState.javaClass, context, null, 0, 0)

    init {
        loadView()

        setUnitStateIfPossible()
    }

    abstract fun loadView()
}