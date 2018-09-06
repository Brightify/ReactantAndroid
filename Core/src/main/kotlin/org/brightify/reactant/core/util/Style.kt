package org.brightify.reactant.core.util

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
@Suppress("FunctionName")
fun <T> Style(closure: T.() -> Unit): T.() -> Unit {
    return closure
}
