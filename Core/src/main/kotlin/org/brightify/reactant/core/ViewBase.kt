package org.brightify.reactant.core

import android.content.Context
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate
import org.brightify.reactant.core.constraint.AutoLayout

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewBase<STATE, ACTION>(context: Context): AutoLayout(context), ComponentWithDelegate<STATE, ACTION> {

    override val lifecycleDisposeBag = CompositeDisposable()

    override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<ACTION>> = emptyList()

    open fun init() {
        componentDelegate.ownerComponent = this

        loadView()
        setupConstraints()

        resetActions()

        afterInit()
        componentDelegate.canUpdate = true
    }

    override fun needsUpdate(): Boolean {
        return true
    }

    override fun afterInit() {
    }

    override fun update() {
    }

    open fun loadView() {
    }

    open fun setupConstraints() {
    }

    protected fun <T: View> make(factory: (Context) -> T): T {
        val view = factory(context)
        (view as? ViewBase<*, *>)?.init()
        return view
    }

    protected fun <P1, T: View> make(factory: (P1, Context) -> T, p1: P1): T {
        return make { context: Context ->
            factory(p1, context)
        }
    }

    protected fun <P1, P2, T: View> make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
        return make { context: Context ->
            factory(p1, p2, context)
        }
    }

    protected fun <P1, P2, P3, T: View> make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
        return make { context: Context ->
            factory(p1, p2, p3, context)
        }
    }

    protected fun <P1, P2, P3, P4, T: View> make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
        return make { context: Context ->
            factory(p1, p2, p3, p4, context)
        }
    }
}
