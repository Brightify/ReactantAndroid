package org.brightify.reactant.core

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.core.component.ComponentDelegate
import org.brightify.reactant.core.component.ComponentWithDelegate


/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
@SuppressLint("ViewConstructor")
open class ViewBase<STATE, ACTION> : AutoLayout(ReactantActivity.context), ComponentWithDelegate<STATE, ACTION>, LifetimeDisposeBagContainerWithDelegate, ViewGroup.OnHierarchyChangeListener {

    final override val componentDelegate = ComponentDelegate<STATE, ACTION>()

    override val actions: List<Observable<out ACTION>> = emptyList()

    override val lifetimeDisposeBagContainerDelegate = LifetimeDisposeBagContainerDelegate { init() }

    fun init() {
        setOnHierarchyChangeListener(HierarchyTreeChangeListener(this))

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

    override fun onChildViewAdded(parent: View?, child: View?) {
        if (child is LifetimeDisposeBagContainer) {
            addChildContainer(child)
        }
    }

    override fun onChildViewRemoved(parent: View?, child: View?) {
    }
}

/**
 * A [hierarchy change listener][ViewGroup.OnHierarchyChangeListener] which recursively
 * monitors an entire tree of views.
 */
class HierarchyTreeChangeListener(private val delegate: ViewGroup.OnHierarchyChangeListener): ViewGroup.OnHierarchyChangeListener {
    override fun onChildViewAdded(parent: View, child: View) {
        delegate.onChildViewAdded(parent, child)

        if (child is ViewGroup) {
            child.setOnHierarchyChangeListener(this)
            for (grandChild in child.children) {
                onChildViewAdded(child, grandChild)
            }
        }
    }

    override fun onChildViewRemoved(parent: View, child: View) {
        if (child is ViewGroup) {
            for (grandChild in child.children) {
                onChildViewRemoved(child, grandChild)
            }
            child.setOnHierarchyChangeListener(null)
        }

        delegate.onChildViewRemoved(parent, child)
    }
}
