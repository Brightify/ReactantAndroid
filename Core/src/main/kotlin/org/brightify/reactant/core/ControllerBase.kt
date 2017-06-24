package org.brightify.reactant.core

import android.annotation.SuppressLint
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.v4.app.SupportActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.brightify.reactant.core.component.Component
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
@SuppressLint("ValidFragment")
open class ControllerBase<STATE, ROOT, ROOT_ACTION>(private val rootViewFactory: (Context) -> ROOT, title: String = "")
    : Fragment(), ComponentWithDelegate<STATE, Unit> where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    final override val lifecycleDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, Unit>()

    final override val action: Observable<Unit> = Observable.empty()

    final override val actions: List<Observable<Unit>> = emptyList()

    var title: String by Delegates.observable(title) { _, _, _ ->
        (activity as? SupportActivity)?.title = title
    }

    lateinit var rootView: ROOT
        private set

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = title
        rootView = rootViewFactory(activity)
        rootView.init()
        rootView.action.subscribe { act(it) }.addTo(lifecycleDisposeBag)

        afterInit()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return rootView
    }

    override fun onStart() {
        super.onStart()

        castRootView?.onStart()
    }

    override fun onResume() {
        super.onResume()

        componentDelegate.canUpdate = true

        castRootView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        componentDelegate.canUpdate = false

        castRootView?.onPause()
    }

    override fun onStop() {
        super.onStop()

        castRootView?.onStop()
    }

    open fun act(action: ROOT_ACTION) {
    }

    final override fun perform(action: Unit) {
    }

    final override fun resetActions() {
    }
}