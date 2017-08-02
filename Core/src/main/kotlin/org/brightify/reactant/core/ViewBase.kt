package org.brightify.reactant.core

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate
import org.brightify.reactant.core.util.makeGuard

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewBase<STATE, ACTION> : AutoLayout(ReactantActivity.context), ComponentWithDelegate<STATE, ACTION> {

    override val lifetimeDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<ACTION>> = emptyList()

    init {
        makeGuard()
    }

    override fun init() {
        componentDelegate.ownerComponent = this

        loadView()
        setupConstraints()

        resetActions()

        afterInit()
        componentDelegate.canUpdate = true
    }

    override fun afterInit() {
    }

    override fun needsUpdate(): Boolean = true

    override fun update() {
    }

    open fun loadView() {
    }

    open fun setupConstraints() {
    }
}
