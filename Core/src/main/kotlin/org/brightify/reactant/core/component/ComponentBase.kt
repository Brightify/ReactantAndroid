package org.brightify.reactant.core.component

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ComponentBase<STATE, ACTION> : ComponentWithDelegate<STATE, ACTION> {

    override val lifecycleDisposeBag = CompositeDisposable()

    override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<ACTION>> = emptyList()

    open val initialCanUpdate: Boolean = true

    override fun init() {
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
