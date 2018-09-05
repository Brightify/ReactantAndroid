package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.LifetimeDisposeBagContainerDelegate
import org.brightify.reactant.core.LifetimeDisposeBagContainerWithDelegate

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class ComponentBase<STATE, ACTION> : ComponentWithDelegate<STATE, ACTION>, LifetimeDisposeBagContainerWithDelegate {

    override val lifetimeDisposeBag = CompositeDisposable()

    override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<ACTION>> = emptyList()

    open val initialCanUpdate: Boolean = true

    override val lifetimeDisposeBagContainerDelegate = LifetimeDisposeBagContainerDelegate { init() }

    fun init() {
        componentDelegate.ownerComponent = this

        resetActions()

        afterInit()

        componentDelegate.canUpdate = initialCanUpdate
    }

    override fun afterInit() {
    }

    override fun needsUpdate(): Boolean {
        return true
    }

    override fun update() {
    }
}
