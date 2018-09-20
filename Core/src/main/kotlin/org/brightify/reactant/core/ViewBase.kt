package org.brightify.reactant.core

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate


/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
@SuppressLint("ViewConstructor")
open class ViewBase<STATE, ACTION>(context: Context, initialState: STATE, layout: Int? = null):
        AutoLayout(context), ComponentWithDelegate<STATE, ACTION>, ComponentView {

    val lifetimeDisposeBag = CompositeDisposable()

    final override val componentDelegate = ComponentDelegate<STATE, ACTION>(initialState)

    override val actions: List<Observable<out ACTION>> = emptyList()

    private var isInitialized: Boolean = false

    private var isDestroyed: Boolean = false

    private val createdViews = ArrayList<ComponentView>()

    init {
        if (layout != null) {
            @Suppress("LeakingThis")
            inflate(context, layout, this)
        }
    }

    override fun init() {
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

    override fun update(previousComponentState: STATE?) {
    }

    open fun loadView() {
    }

    open fun setupConstraints() {
    }

    override fun destroy() {
        isDestroyed = true
        createdViews.forEach(ComponentView::destroy)
        lifetimeDisposeBag.dispose()
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

    protected fun <V: ComponentView> create(factory: (Context) -> V): V {
        val view = factory(context)
        view.init()
        createdViews.add(view)
        return view
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <V: View> create(id: Int): V = LayoutInflater.from(context).inflate(id, this, false) as V

    protected fun <V: View> find(id: Int): V = findViewById(id)

    protected fun Int.toPx() = this * resources.displayMetrics.density
}
