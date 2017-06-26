package org.brightify.reactant.core

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate
import org.brightify.reactant.core.constraint.AutoLayout

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewBase<STATE, ACTION>(context: Context) : AutoLayout(context), ComponentWithDelegate<STATE, ACTION> {

    override val lifecycleDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<ACTION>> = emptyList()

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
