package org.brightify.reactant.core.component

import io.reactivex.Observable

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class ComponentBase<STATE, ACTION>(initialState: STATE) : ComponentWithDelegate<STATE, ACTION> {

    override val componentDelegate = ComponentDelegate<STATE, ACTION>(initialState)

    override val actions: List<Observable<ACTION>> = emptyList()

    open val initialCanUpdate: Boolean = true

    fun init() {
        componentDelegate.ownerComponent = this

        resetActions()

        afterInit()

        componentDelegate.canUpdate = initialCanUpdate
    }

    open fun afterInit() {
    }

    override fun needsUpdate(): Boolean {
        return true
    }

    override fun update(previousComponentState: STATE?) {
    }
}
