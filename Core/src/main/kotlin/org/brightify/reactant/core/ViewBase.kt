package org.brightify.reactant.core

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.support.annotation.StyleableRes
import android.support.v4.content.res.TypedArrayUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
@SuppressLint("ViewConstructor")
open class ViewBase<STATE, ACTION> @JvmOverloads constructor(
    context: Context, initialState: STATE, layout: Int? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0):
        AutoLayout(context, attrs, defStyleAttr, defStyleRes), ComponentWithDelegate<STATE, ACTION>, ComponentView {

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

    fun AttributeSet.read(@StyleableRes attrId: IntArray, closure: (TypedArray) -> Unit) {
        val attributes = context.obtainStyledAttributes(this, attrId)
        try {
            closure(attributes)
        } finally {
            attributes.recycle()
        }
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

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        forEachComponentView(child) {
            createdViews.add(it)
        }
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)

        forEachComponentView(child) {
            createdViews.remove(it)
        }
    }

    private fun forEachComponentView(view: View?, closure: (ComponentView) -> Unit) {
        when (view) {
            null -> return
            is ComponentView -> closure(view)
            is ViewGroup -> view.children.forEach { forEachComponentView(it, closure) }
        }
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
//        createdViews.add(view)
        return view
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <V: View> create(id: Int): V = LayoutInflater.from(context).inflate(id, this, false) as V

    protected fun <V: View> find(id: Int): V = findViewById(id)

    protected fun Int.toPx() = this * resources.displayMetrics.density

    protected fun Int.fromPx() = this / resources.displayMetrics.density
}
