package org.brightify.reactant.core

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.core.component.Component

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class ScrollControllerBase<STATE, ROOT, ROOT_ACTION>(initialState: STATE, rootViewFactory: (Context) -> ROOT) : ControllerBase<STATE, ROOT, ROOT_ACTION>(
        initialState, rootViewFactory) where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    @Suppress("UNCHECKED_CAST")
    override val rootView: ROOT
        get() = scrollView.getChildAt(0) as ROOT

    private val scrollView: ScrollView
        get() = super.rootView as ScrollView


    override fun loadView() {
        super.loadView()

        view = ScrollView(activity).children(rootView)
        rootView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }
}