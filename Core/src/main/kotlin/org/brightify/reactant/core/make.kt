package org.brightify.reactant.core

import android.content.Context
import android.view.View
import org.brightify.reactant.core.component.Component

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
typealias FactoryWithContext<T> = (Context) -> T

var __makeUsed = ThreadLocal<Int>()

class MakeNotUsedException : RuntimeException("Some Component was not initialized using make().")

class MakeGuardNotUsedException : RuntimeException("Some Component did not call makeGuard() in its init.")

fun Component<*, *>.makeGuard() {
    if (__makeUsed.get() == null || __makeUsed.get() < 1) {
        throw MakeNotUsedException()
    }
    __makeUsed.set(__makeUsed.get() - 1)
}

inline fun <T> make(factory: () -> T): T {
    val makeUsedCount = __makeUsed.get() ?: 0
    __makeUsed.set(makeUsedCount + 1)
    return factory().also {
        if (it is Component<*, *>) {
            if (__makeUsed.get() != makeUsedCount) {
                throw MakeGuardNotUsedException()
            }
            it.init()
        } else {
            __makeUsed.set(makeUsedCount)
        }
    }
}

inline fun <P1, T> make(factory: (P1) -> T, p1: P1): T {
    return make { -> factory(p1) }
}

inline fun <P1, P2, T> make(factory: (P1, P2) -> T, p1: P1, p2: P2): T {
    return make { -> factory(p1, p2) }
}

inline fun <P1, P2, P3, T> make(factory: (P1, P2, P3) -> T, p1: P1, p2: P2, p3: P3): T {
    return make { -> factory(p1, p2, p3) }
}

inline fun <P1, P2, P3, P4, T> make(factory: (P1, P2, P3, P4) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return make { -> factory(p1, p2, p3, p4) }
}

inline fun <T> make(crossinline factory: (Context) -> T): FactoryWithContext<T> {
    return { it.make(factory) }
}

inline fun <P1, T> make(crossinline factory: (P1, Context) -> T, p1: P1): FactoryWithContext<T> {
    return { it.make(factory, p1) }
}

inline fun <P1, P2, T> make(crossinline factory: (P1, P2, Context) -> T, p1: P1, p2: P2): FactoryWithContext<T> {
    return { it.make(factory, p1, p2) }
}

inline fun <P1, P2, P3, T> make(crossinline factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): FactoryWithContext<T> {
    return { it.make(factory, p1, p2, p3) }
}

inline fun <P1, P2, P3, P4, T> make(crossinline factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3,
                                    p4: P4): FactoryWithContext<T> {
    return { it.make(factory, p1, p2, p3, p4) }
}

inline fun <T> Context.make(factory: (Context) -> T): T {
    return make { -> factory(this) }
}

inline fun <P1, T> Context.make(factory: (P1, Context) -> T, p1: P1): T {
    return make { -> factory(p1, this) }
}

inline fun <P1, P2, T> Context.make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return make { -> factory(p1, p2, this) }
}

inline fun <P1, P2, P3, T> Context.make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
    return make { -> factory(p1, p2, p3, this) }
}

inline fun <P1, P2, P3, P4, T> Context.make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return make { -> factory(p1, p2, p3, p4, this) }
}

inline fun <T> View.make(factory: (Context) -> T): T {
    return make { -> factory(context) }
}

inline fun <P1, T> View.make(factory: (P1, Context) -> T, p1: P1): T {
    return make { -> factory(p1, context) }
}

inline fun <P1, P2, T> View.make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return make { -> factory(p1, p2, context) }
}

inline fun <P1, P2, P3, T> View.make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
    return make { -> factory(p1, p2, p3, context) }
}

inline fun <P1, P2, P3, P4, T> View.make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return make { -> factory(p1, p2, p3, p4, context) }
}

inline fun <T : Component<STATE, *>, reified STATE> makeComponent(factory: () -> T): T {
    return make(factory).also {
        if (STATE::class == Unit::class) {
            @Suppress("UNCHECKED_CAST")
            it.componentState = Unit as STATE
        }
    }
}

inline fun <P1, T : Component<STATE, *>, reified STATE> makeComponent(factory: (P1) -> T, p1: P1): T {
    return makeComponent { -> factory(p1) }
}

inline fun <P1, P2, T : Component<STATE, *>, reified STATE> makeComponent(factory: (P1, P2) -> T, p1: P1, p2: P2): T {
    return makeComponent { -> factory(p1, p2) }
}

inline fun <P1, P2, P3, T : Component<STATE, *>, reified STATE> makeComponent(factory: (P1, P2, P3) -> T, p1: P1, p2: P2, p3: P3): T {
    return makeComponent { -> factory(p1, p2, p3) }
}

inline fun <P1, P2, P3, P4, T : Component<STATE, *>, reified STATE> makeComponent(factory: (P1, P2, P3, P4) -> T, p1: P1, p2: P2, p3: P3,
                                                                                  p4: P4): T {
    return makeComponent { -> factory(p1, p2, p3, p4) }
}

inline fun <T : Component<STATE, *>, reified STATE> makeComponent(crossinline factory: (Context) -> T): FactoryWithContext<T> {
    return { it.makeComponent(factory) }
}

inline fun <P1, T : Component<STATE, *>, reified STATE> makeComponent(crossinline factory: (P1, Context) -> T,
                                                                      p1: P1): FactoryWithContext<T> {
    return { it.makeComponent(factory, p1) }
}

inline fun <P1, P2, T : Component<STATE, *>, reified STATE> makeComponent(crossinline factory: (P1, P2, Context) -> T, p1: P1,
                                                                          p2: P2): FactoryWithContext<T> {
    return { it.makeComponent(factory, p1, p2) }
}

inline fun <P1, P2, P3, T : Component<STATE, *>, reified STATE> makeComponent(crossinline factory: (P1, P2, P3, Context) -> T, p1: P1,
                                                                              p2: P2,
                                                                              p3: P3): FactoryWithContext<T> {
    return { it.makeComponent(factory, p1, p2, p3) }
}

inline fun <P1, P2, P3, P4, T : Component<STATE, *>, reified STATE> makeComponent(crossinline factory: (P1, P2, P3, P4, Context) -> T,
                                                                                  p1: P1,
                                                                                  p2: P2, p3: P3,
                                                                                  p4: P4): FactoryWithContext<T> {
    return { it.makeComponent(factory, p1, p2, p3, p4) }
}

inline fun <T : Component<STATE, *>, reified STATE> Context.makeComponent(factory: (Context) -> T): T {
    return makeComponent { -> factory(this) }
}

inline fun <P1, T : Component<STATE, *>, reified STATE> Context.makeComponent(factory: (P1, Context) -> T, p1: P1): T {
    return makeComponent { -> factory(p1, this) }
}

inline fun <P1, P2, T : Component<STATE, *>, reified STATE> Context.makeComponent(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return makeComponent { -> factory(p1, p2, this) }
}

inline fun <P1, P2, P3, T : Component<STATE, *>, reified STATE> Context.makeComponent(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2,
                                                                                      p3: P3): T {
    return makeComponent { -> factory(p1, p2, p3, this) }
}

inline fun <P1, P2, P3, P4, T : Component<STATE, *>, reified STATE> Context.makeComponent(factory: (P1, P2, P3, P4, Context) -> T, p1: P1,
                                                                                          p2: P2,
                                                                                          p3: P3, p4: P4): T {
    return makeComponent { -> factory(p1, p2, p3, p4, this) }
}

inline fun <T : Component<STATE, *>, reified STATE> View.makeComponent(factory: (Context) -> T): T {
    return makeComponent { -> factory(context) }
}

inline fun <P1, T : Component<STATE, *>, reified STATE> View.makeComponent(factory: (P1, Context) -> T, p1: P1): T {
    return makeComponent { -> factory(p1, context) }
}

inline fun <P1, P2, T : Component<STATE, *>, reified STATE> View.makeComponent(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return makeComponent { -> factory(p1, p2, context) }
}

inline fun <P1, P2, P3, T : Component<STATE, *>, reified STATE> View.makeComponent(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2,
                                                                                   p3: P3): T {
    return makeComponent { -> factory(p1, p2, p3, context) }
}

inline fun <P1, P2, P3, P4, T : Component<STATE, *>, reified STATE> View.makeComponent(factory: (P1, P2, P3, P4, Context) -> T, p1: P1,
                                                                                       p2: P2,
                                                                                       p3: P3, p4: P4): T {
    return makeComponent { -> factory(p1, p2, p3, p4, context) }
}
