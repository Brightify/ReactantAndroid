package org.brightify.reactant.core

import android.content.Context
import android.view.View
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import org.brightify.reactant.controller.ViewController
import org.brightify.reactant.core.component.Component
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class ControllerBase<STATE, ROOT, ROOT_ACTION>(initialState: STATE, private val rootViewFactory: (Context) -> ROOT)
    : ViewController(), ComponentWithDelegate<STATE, Unit> where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    final override val componentDelegate = ComponentDelegate<STATE, Unit>(initialState)

    final override val action: Observable<Unit> = Observable.empty()

    final override val actions: List<Observable<Unit>> = emptyList()

    @Suppress("UNCHECKED_CAST")
    open val rootView: ROOT
        get() = view as ROOT

    private val castRootView: RootView?
        get() = rootView as? RootView

    private var rootViewState: StateWrapper = StateWrapper.NoState

    sealed class StateWrapper {
        class HasState(val state: Any?) : StateWrapper()
        object NoState : StateWrapper()
    }

    init {
        componentDelegate.ownerComponent = this
    }

    override fun needsUpdate(): Boolean = true

    override fun update() {
    }

    override fun loadView() {
        super.loadView()

        val newRootView = rootViewFactory(activity)

        (newRootView as? ViewBase<*, *>)?.init()

        (rootViewState as? StateWrapper.HasState)?.let { rootViewState ->
            if (newRootView.componentState != rootViewState) {
                @Suppress("UNCHECKED_CAST")
                (newRootView as Component<Any?, *>).componentState = rootViewState.state
            }
        }

        newRootView.observableState.subscribe { rootViewState = StateWrapper.HasState(it) }.addTo(viewLifecycleDisposeBag)
        newRootView.action.subscribe { act(it) }.addTo(viewLifecycleDisposeBag)

        view = newRootView
    }

    override fun viewWillAppear() {
        super.viewWillAppear()

        castRootView?.viewWillAppear()
    }

    override fun viewDidAppear() {
        super.viewDidAppear()

        componentDelegate.canUpdate = true

        castRootView?.viewDidAppear()
    }

    override fun viewWillDisappear() {
        super.viewWillDisappear()

        componentDelegate.canUpdate = false

        castRootView?.viewWillDisappear()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()

        castRootView?.viewDidDisappear()
    }

    override fun viewDestroyed() {
        super.viewDestroyed()

        stateDisposeBag.clear()
    }

    open fun act(action: ROOT_ACTION) {
    }

    final override fun perform(action: Unit) {
    }

    final override fun resetActions() {
    }
}