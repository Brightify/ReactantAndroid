package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class ComponentDelegate<STATE, ACTION>(val initialState: STATE) {

    val stateDisposeBag = CompositeDisposable()

    val observableState: Observable<STATE>
        get() = observableStateSubject

    var temporaryPreviousComponentState: STATE? = null
        private set

    var componentState: STATE by Delegates.observable(initialState) { _, oldValue, _ ->
        temporaryPreviousComponentState = oldValue
        needsUpdate = true
    }

    val action: Observable<ACTION>
        get() = actionSubject

    var needsUpdate: Boolean by Delegates.observable(false) { _, _, _ ->
        if (needsUpdate && canUpdate) {
            update()
        }
    }

    var canUpdate: Boolean by Delegates.observable(false) { _, _, _ ->
        if (needsUpdate && canUpdate) {
            update()
        }
    }

    var ownerComponent: Component<STATE, ACTION>? by Delegates.observable(null) { _, _, _ ->
        needsUpdate = true
    }

    var actions: List<Observable<out ACTION>> by Delegates.observable(emptyList()) { _, _, _ ->
        actionsDisposeBag.clear()
        Observable.merge(actions).subscribe(this::perform).addTo(actionsDisposeBag)
    }

    private val observableStateSubject = ReplaySubject.create<STATE>(1)
    private val actionSubject = PublishSubject.create<ACTION>()

    private val actionsDisposeBag = CompositeDisposable()

    fun perform(action: ACTION) {
        actionSubject.onNext(action)
    }

    private fun update() {
        needsUpdate = false

        if (ownerComponent == null) {
            throw UnsupportedOperationException("Owner component must be set before update is called.")
        }

        if (ownerComponent?.needsUpdate() == true) {
            stateDisposeBag.clear()
            ownerComponent?.update(temporaryPreviousComponentState)
            observableStateSubject.onNext(componentState)
            temporaryPreviousComponentState = null
        }
    }
}
