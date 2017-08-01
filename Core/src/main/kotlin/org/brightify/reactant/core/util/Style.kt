package org.brightify.reactant.core.util

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun <T> Style(closure: T.() -> Unit): T.() -> Unit {
    return closure
}
