package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class ComponentDelegate<STATE, ACTION>() {
    val stateDisposeBag = CompositeDisposable()

    val observableState: Observable<STATE>
        get() = observableStateSubject

    var previousComponentState: STATE? = null
        private set

    var componentState: STATE
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

    var ownerComponent: Component<STATE, ACTION>? by object : ObservableProperty<Component<STATE, ACTION>?>(null) {

        override fun afterChange(property: KProperty<*>, oldValue: Component<STATE, ACTION>?, newValue: Component<STATE, ACTION>?) {
            needsUpdate = stateStorage != null
        }
    }

    var actions: List<Observable<ACTION>> by Delegates.observable(emptyList()) { _, _, _ ->
        actionsDisposeBag.clear()
        Observable.merge(actions).subscribe(this::perform).addTo(actionsDisposeBag)
    }

    val hasComponentState: Boolean
        get() = stateStorage != null

    private val observableStateSubject = ReplaySubject.create<STATE>(1)
    private val actionSubject = PublishSubject.create<ACTION>()

    private var stateStorage: STATE? = null

    private val actionsDisposeBag = CompositeDisposable()

    fun perform(action: ACTION) {
        actionSubject.onNext(action)
    }

    private fun update() {
        // TODO Exceptions
        if (stateStorage == null) {
            throw UnsupportedOperationException()
        }

        needsUpdate = false

        if (ownerComponent == null) {
            throw UnsupportedOperationException()
        }

        if (ownerComponent?.needsUpdate() == true) {
            stateDisposeBag.clear()
            ownerComponent?.update()
        }
    }
}