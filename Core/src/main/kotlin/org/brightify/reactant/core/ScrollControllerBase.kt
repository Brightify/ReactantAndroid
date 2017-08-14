package org.brightify.reactant.core

import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.core.component.Component

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ScrollControllerBase<STATE, ROOT, ROOT_ACTION>(rootView: ROOT, title: String = "") : ControllerBase<STATE, ROOT, ROOT_ACTION>(
        rootView, title) where ROOT : View, ROOT : Component<*, ROOT_ACTION> {

    @Suppress("UNCHECKED_CAST")
    override val rootView: ROOT
        get() = scrollView.getChildAt(0) as ROOT

    private val scrollView: ScrollView
        get() = super.rootView as ScrollView

    init {
        view = ScrollView(ReactantActivity.context).children(rootView)
        rootView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    }
}