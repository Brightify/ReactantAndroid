package org.brightify.reactant.core

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
open class ControllerBase<STATE, ROOT, ROOT_ACTION>(rootView: ROOT)
    : ViewController(), ComponentWithDelegate<STATE, Unit> where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    final override val componentDelegate = ComponentDelegate<STATE, Unit>()

    final override val action: Observable<Unit> = Observable.empty()

    final override val actions: List<Observable<Unit>> = emptyList()

    @Suppress("UNCHECKED_CAST")
    open val rootView: ROOT
        get() = view as ROOT

    private val castRootView: RootView?
        get() = rootView as? RootView

    init {
        view = rootView
    }

    override fun init() {
        super.init()

        componentDelegate.ownerComponent = this

        onDispose.subscribe { stateDisposeBag.dispose() }.addTo(lifetimeDisposeBag)

        addChildContainer(rootView)

        afterInit()
    }

    override fun afterInit() {
    }

    override fun needsUpdate(): Boolean = true

    override fun update() {
    }

    override fun loadView() {
        super.loadView()

        rootView.action.subscribe { act(it) }.addTo(lifetimeDisposeBag)
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

    open fun act(action: ROOT_ACTION) {
    }

    final override fun perform(action: Unit) {
    }

    final override fun resetActions() {
    }
}