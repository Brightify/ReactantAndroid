package org.brightify.reactant

import rx.Observable
import rx.subjects.ReplaySubject
import rx.subscriptions.CompositeSubscription

/**
 * Created by TadeasKriz on 12/09/16.
 */
class ComponentBase<STATE: Any>(override val stateType: Class<STATE>): Component<STATE> {
    val lifecycleDisposeBag = CompositeSubscription()
    var stateDisposeBag = CompositeSubscription()

    override val observableState: Observable<STATE>
        get() = observableStateSubject
    private val observableStateSubject = ReplaySubject.create<STATE>(1)

    override var previousComponentState: STATE? = null
        private set

    private var stateStorage: STATE? = null
    override var componentState: STATE
        get() {
            val state = stateStorage
            if (state != null) {
                return state
            } else {
                throw UnsupportedOperationException()
            }
        }
        set(value) {
            previousComponentState = stateStorage
            stateStorage = value
            observableStateSubject.onNext(value)
            stateDisposeBag.clear()
            render()
        }

    override fun render() {  }
}
