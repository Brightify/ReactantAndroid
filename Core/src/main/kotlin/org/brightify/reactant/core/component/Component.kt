package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.LifetimeDisposeBagContainer

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
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

fun <STATE, ACTION, COMPONENT: Component<STATE, ACTION>> COMPONENT.withState(state: STATE): COMPONENT {
    setComponentState(state)
    return this
}

fun <STATE: MutableComponentState<STATE>>Component<STATE, *>.mutateState(mutation: STATE.() -> Unit) {
    val copy = componentState.clone()
    mutation(copy)
    setComponentState(copy)
}
