package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.LifetimeDisposeBagContainer
import org.brightify.reactant.core.LifetimeDisposeBagContainerDelegate
import org.brightify.reactant.core.LifetimeDisposeBagContainerWithDelegate
import org.brightify.reactant.core.util.makeGuard

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ComponentBase<STATE, ACTION> : ComponentWithDelegate<STATE, ACTION>, LifetimeDisposeBagContainerWithDelegate {

    override val lifetimeDisposeBag = CompositeDisposable()

    override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<ACTION>> = emptyList()

    open val initialCanUpdate: Boolean = true

    override val lifetimeDisposeBagContainerDelegate = LifetimeDisposeBagContainerDelegate { init() }

    init {
        makeGuard()
    }

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
