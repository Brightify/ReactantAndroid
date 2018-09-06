package org.brightify.reactant.core

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate


/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
@SuppressLint("ViewConstructor")
open class ViewBase<STATE, ACTION>(context: Context, initialState: STATE): AutoLayout(context), ComponentWithDelegate<STATE, ACTION> {

    val lifetimeDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, ACTION>(initialState)

    override val actions: List<Observable<out ACTION>> = emptyList()

    private var isInitialized: Boolean = false

    private var isDestroyed: Boolean = false

    private val createdViews = ArrayList<ViewBase<*, *>>()

    fun init() {
        isInitialized = true
        componentDelegate.ownerComponent = this

        loadView()
        setupConstraints()

        resetActions()

        afterInit()
        componentDelegate.canUpdate = true
    }

    open fun afterInit() {
    }

    override fun needsUpdate(): Boolean = true

    override fun update() {
    }

    open fun loadView() {
    }

    open fun setupConstraints() {
    }

    open fun destroy() {
        isDestroyed = true
        createdViews.forEach(ViewBase<*, *>::destroy)
        lifetimeDisposeBag.dispose()
    }

    fun <V: ViewBase<*, *>> ViewBase<*, *>.create(factory: (Context) -> V): V {
        val view = factory(context)
        view.init()
        createdViews.add(view)
        return view
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInitialized) {
            throw IllegalStateException("View must be initialized before it is added to view hierarchy. " +
                    "Probably caused by creating view via constructor instead of 'create' from view or by factory in controller.")
        } else if (isDestroyed) {
            throw IllegalStateException("View cannot be added to view hierarchy after it is destroyed.")
        }
    }
}
