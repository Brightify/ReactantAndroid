package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
interface ComponentWithDelegate<STATE, ACTION> : Component<STATE, ACTION> {

    val componentDelegate: ComponentDelegate<STATE, ACTION>

    val actions: List<Observable<out ACTION>>

    override val action: Observable<ACTION>
        get() = componentDelegate.action

    override val stateDisposeBag: CompositeDisposable
        get() = componentDelegate.stateDisposeBag

    override val observableState: Observable<STATE>
        get() = componentDelegate.observableState

    override val previousComponentState: STATE?
        get() = componentDelegate.previousComponentState

    override var componentState: STATE
        get() = componentDelegate.componentState
        set(value) {
            componentDelegate.componentState = value
        }

    override fun perform(action: ACTION) {
        componentDelegate.perform(action)
    }

    fun resetActions() {
        componentDelegate.actions = actions
    }
}

fun <STATE, ACTION> ComponentWithDelegate<STATE, ACTION>.invalidate() {
    if (componentDelegate.hasComponentState) {
        componentDelegate.needsUpdate = true
    }
}