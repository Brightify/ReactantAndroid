package org.brightify.reactant.core.util

import android.util.Log

/**
 * @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
fun assertLog(assertion: Boolean, message: String) {
    assert(assertion) { message }
    Log.e("Assertion failed", message)
}