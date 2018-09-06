@file:Suppress("FunctionName")

package org.brightify.reactant.core.util

import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import org.brightify.reactant.autolayout.AutoLayout
import org.brightify.reactant.core.ViewBase

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
fun ViewBase<*, *>.View(): View {
    return View(context)
}

fun ViewBase<*, *>.ImageView(): ImageView {
    return ImageView(context)
}

fun ViewBase<*, *>.ImageButton(): ImageButton {
    return ImageButton(context)
}

fun ViewBase<*, *>.TextView(): TextView {
    return TextView(context)
}

fun ViewBase<*, *>.TextView(text: String): TextView {
    return TextView().apply { this.text = text }
}

fun ViewBase<*, *>.Button(): Button {
    return Button(context)
}

fun ViewBase<*, *>.Button(text: String): Button {
    return Button().apply { this.text = text }
}

fun ViewBase<*, *>.EditText(): EditText {
    return EditText(context)
}

fun ViewBase<*, *>.TextInputEditText(): TextInputEditText {
    return TextInputEditText(context)
}

fun ViewBase<*, *>.ProgressBar(): ProgressBar {
    return ProgressBar(context)
}

fun ViewBase<*, *>.Switch(): Switch {
    return Switch(context)
}

fun ViewBase<*, *>.AutoLayout(): AutoLayout {
    return AutoLayout(context)
}

fun ViewBase<*, *>.ScrollView(): ScrollView {
    return ScrollView(context)
}

fun ViewBase<*, *>.FrameLayout(): FrameLayout {
    return FrameLayout(context)
}

fun ViewBase<*, *>.RecyclerView(): RecyclerView {
    return RecyclerView(context)
}

fun ViewBase<*, *>.SwipeRefreshLayout(): SwipeRefreshLayout {
    return SwipeRefreshLayout(context)
}

fun ViewBase<*, *>.TextInputLayout(): TextInputLayout {
    return TextInputLayout(context)
}

fun ViewBase<*, *>.FloatingActionButton(): FloatingActionButton {
    return FloatingActionButton(context)
}