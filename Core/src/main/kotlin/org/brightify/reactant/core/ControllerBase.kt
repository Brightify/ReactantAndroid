package org.brightify.reactant.core

import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.brightify.reactant.core.component.Component
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate
import org.brightify.reactant.controller.ViewController
import org.brightify.reactant.core.util.makeGuard

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ControllerBase<STATE, ROOT, ROOT_ACTION>(rootView: ROOT, title: String = "")
    : ViewController(), ComponentWithDelegate<STATE, Unit> where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    final override val lifetimeDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, Unit>()

    final override val action: Observable<Unit> = Observable.empty()

    final override val actions: List<Observable<Unit>> = emptyList()

    @Suppress("UNCHECKED_CAST")
    val rootView: ROOT
        get() = view as ROOT

    private val castRootView: RootView?
        get() = rootView as? RootView

    init {
        makeGuard()

        view = rootView
    }

    override fun init() {
        componentDelegate.ownerComponent = this
    }

    override fun afterInit() {
    }

    override fun needsUpdate(): Boolean = true

    override fun update() {
    }

    override fun loadView() {
        super.loadView()

        rootView.action.subscribe { act(it) }.addTo(lifetimeDisposeBag)

        afterInit()
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

        castRootView?.viewWillDisapper()
    }

    override fun viewDidDisappear() {
        super.viewDidDisappear()

        castRootView?.viewDidDisapper()
    }

    open fun act(action: ROOT_ACTION) {
    }

    final override fun perform(action: Unit) {
    }

    final override fun resetActions() {
    }
}