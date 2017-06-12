package org.brightify.reactant.core

import android.content.Context
import android.widget.Button
import android.widget.TextView

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun TextView(text: String, context: Context): TextView {
    return TextView(context).apply { this.text = text }
}

fun Button(text: String, context: Context): Button {
    return Button(context).apply { this.text = text }
}