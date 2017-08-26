package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.LifetimeDisposeBagContainer

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
interface Component<STATE, ACTION>: LifetimeDisposeBagContainer {
    val stateDisposeBag: CompositeDisposable

    val observableState: Observable<STATE>

    val previousComponentState: STATE?

    var componentState: STATE

    val action: Observable<ACTION>

    fun afterInit()

    fun needsUpdate(): Boolean

    fun update()

    fun perform(action: ACTION)
}

fun <STATE, ACTION> Component<STATE, ACTION>.setComponentState(state: STATE) {
    componentState = state
}

fun <STATE, ACTION> Component<STATE, ACTION>.withState(state: STATE): Component<STATE, ACTION> {
    setComponentState(state)
    return this
}

fun <STATE>Component<STATE, *>.mutateState(mutation: STATE.() -> Unit) {
    mutation(componentState)
    setComponentState(componentState)
}
