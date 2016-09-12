package org.brightify.reactant

import rx.Observable

/**
 * Created by TadeasKriz on 12/09/16.
 */
interface Component<STATE> {
    val stateType: Class<STATE>

    val observableState: Observable<STATE>

    val previousComponentState: STATE?

    var componentState: STATE

    fun render()
}

fun <STATE> Component<STATE>.setComponentState(state: STATE) {
    componentState = state
}

fun <COMPONENT: Component<STATE>, STATE> COMPONENT.withState(state: STATE): COMPONENT {
    setComponentState(state)
    return this
}

fun <STATE: Any> Component<STATE>.setUnitStateIfPossible() {
    if (stateType.isInstance(Unit)) {
        componentState = stateType.cast(Unit)
    }
}