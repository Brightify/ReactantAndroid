@file:Suppress("FunctionName")

package org.brightify.reactant.core.util

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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.brightify.reactant.autolayout.AutoLayout

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
fun View.View(): View {
    return View(context)
}

fun View.ImageView(): ImageView {
    return ImageView(context)
}

fun View.ImageButton(): ImageButton {
    return ImageButton(context)
}

fun View.TextView(): TextView {
    return TextView(context)
}

fun View.TextView(text: String): TextView {
    return TextView().apply { this.text = text }
}

fun View.Button(): Button {
    return Button(context)
}

fun View.Button(text: String): Button {
    return Button().apply { this.text = text }
}

fun View.EditText(): EditText {
    return EditText(context)
}

fun View.TextInputEditText(): TextInputEditText {
    return TextInputEditText(context)
}

fun View.ProgressBar(): ProgressBar {
    return ProgressBar(context)
}

fun View.Switch(): Switch {
    return Switch(context)
}

fun View.AutoLayout(): AutoLayout {
    return AutoLayout(context)
}

fun View.ScrollView(): ScrollView {
    return ScrollView(context)
}

fun View.FrameLayout(): FrameLayout {
    return FrameLayout(context)
}

fun View.RecyclerView(): RecyclerView {
    return RecyclerView(context)
}

fun View.SwipeRefreshLayout(): SwipeRefreshLayout {
    return SwipeRefreshLayout(context)
}

fun View.TextInputLayout(): TextInputLayout {
    return TextInputLayout(context)
}

fun View.FloatingActionButton(): FloatingActionButton {
    return FloatingActionButton(context)
}