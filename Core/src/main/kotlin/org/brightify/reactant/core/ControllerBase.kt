package org.brightify.reactant.core

import android.support.v4.app.SupportActivity
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.brightify.reactant.core.component.Component
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate
import org.brightify.reactant.core.constraint.AutoLayout
import org.brightify.reactant.core.constraint.ConstraintPriority
import org.brightify.reactant.core.constraint.util.snp
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ControllerBase<STATE, ROOT, ROOT_ACTION>(private val rootViewFactory: FactoryWithContext<ROOT>, title: String = "")
    : ViewController(), ComponentWithDelegate<STATE, Unit> where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    final override val lifecycleDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, Unit>()

    final override val action: Observable<Unit> = Observable.empty()

    final override val actions: List<Observable<Unit>> = emptyList()

    var title: String by Delegates.observable(title) { _, _, _ ->
        (activity as? SupportActivity)?.title = title
    }

    @Suppress("UNCHECKED_CAST")
    val rootView: ROOT
        get() = contentView as ROOT

    private val castRootView: RootView?
        get() = rootView as? RootView

    override fun init() {
        componentDelegate.ownerComponent = this
    }

    override fun afterInit() {
    }

    override fun needsUpdate(): Boolean = true

    override fun update() {
    }

    override fun onCreate() {
        title = title
        contentView = rootViewFactory(activity)
        rootView.action.subscribe { act(it) }.addTo(lifecycleDisposeBag)
        (rootView as? AutoLayout)?.let {
            it.snp.verticalContentCompressionResistancePriority = ConstraintPriority.high
            it.snp.horizontalContentCompressionResistancePriority = ConstraintPriority.high
        }

        afterInit()
    }

    override fun onStart() {
        castRootView?.onStart()
    }

    override fun onResume() {
        componentDelegate.canUpdate = true

        castRootView?.onResume()
    }

    override fun onPause() {
        componentDelegate.canUpdate = false

        castRootView?.onPause()
    }

    override fun onStop() {
        castRootView?.onStop()
    }

    open fun act(action: ROOT_ACTION) {
    }

    final override fun perform(action: Unit) {
    }

    final override fun resetActions() {
    }
}