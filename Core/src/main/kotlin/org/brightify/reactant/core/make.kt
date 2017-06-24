package org.brightify.reactant.core

import android.content.Context
import android.view.View

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun <T: View> make(factory: (Context) -> T): (Context) -> T {
    return factory
}

fun <P1, T: View> make(factory: (P1, Context) -> T, p1: P1): (Context) -> T {
    return make { context: Context ->
        factory(p1, context)
    }
}

fun <P1, P2, T: View> make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): (Context) -> T {
    return make { context: Context ->
        factory(p1, p2, context)
    }
}

fun <P1, P2, P3, T: View> make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): (Context) -> T {
    return make { context: Context ->
        factory(p1, p2, p3, context)
    }
}

fun <P1, P2, P3, P4, T: View> make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): (Context) -> T {
    return make { context: Context ->
        factory(p1, p2, p3, p4, context)
    }
}
