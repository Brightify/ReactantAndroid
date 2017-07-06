package org.brightify.reactant.core

import android.content.Context
import android.util.Log
import android.view.View
import org.brightify.reactant.core.component.Component
import org.brightify.reactant.core.component.setComponentState
import java.util.*

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
typealias FactoryWithContext<T> = (Context) -> T

private var makeUsed = ThreadLocal<Int>()

class MakeNotUsedException: RuntimeException("Some Component was not initialized using make().")

class MakeGuardNotUsedException: RuntimeException("Some Component did not call makeGuard() in its init.")

fun Component<*, *>.makeGuard() {
    if (makeUsed.get() == null || makeUsed.get() < 1) {
        throw MakeNotUsedException()
    }
    makeUsed.set(makeUsed.get() - 1)
}

fun <T> make(factory: () -> T): T {
    val makeUsedCount = makeUsed.get() ?: 0
    makeUsed.set(makeUsedCount + 1)
    return factory().also {
        if (it is Component<*, *>) {
            if (makeUsed.get() != makeUsedCount) {
                throw MakeGuardNotUsedException()
            }
            it.init()
        } else {
            makeUsed.set(makeUsedCount)
        }
    }
}

inline fun <T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: () -> T): T {
    return make(factory).also { prepareComponent(it, STATE::class.java) }
}

fun <P1, T> make(factory: (P1) -> T, p1: P1): T {
    return make { ->
        factory(p1)
    }
}

inline fun <P1, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1) -> T, p1: P1): T {
    return make(factory, p1).also { prepareComponent(it, STATE::class.java) }
}

fun <P1, P2, T> make(factory: (P1, P2) -> T, p1: P1, p2: P2): T {
    return make { ->
        factory(p1, p2)
    }
}

inline fun <P1, P2, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, P2) -> T, p1: P1, p2: P2): T {
    return make(factory, p1, p2).also { prepareComponent(it, STATE::class.java) }
}

fun <P1, P2, P3, T> make(factory: (P1, P2, P3) -> T, p1: P1, p2: P2, p3: P3): T {
    return make { ->
        factory(p1, p2, p3)
    }
}

inline fun <P1, P2, P3, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, P2, P3) -> T, p1: P1, p2: P2, p3: P3): T {
    return make(factory, p1, p2, p3).also { prepareComponent(it, STATE::class.java) }
}

fun <P1, P2, P3, P4, T> make(factory: (P1, P2, P3, P4) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return make { ->
        factory(p1, p2, p3, p4)
    }
}

inline fun <P1, P2, P3, P4, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, P2, P3, P4) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return make(factory, p1, p2, p3, p4).also { prepareComponent(it, STATE::class.java) }
}

fun <T> make(factory: (Context) -> T): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory)
    }
}

inline fun <T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (Context) -> T): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory).also { prepareComponent(it, STATE::class.java) }
    }
}

fun <P1, T> make(factory: (P1, Context) -> T, p1: P1): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1)
    }
}

inline fun <P1, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, Context) -> T, p1: P1): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1).also { prepareComponent(it, STATE::class.java) }
    }
}

fun <P1, P2, T> make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1, p2)
    }
}

inline fun <P1, P2, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, P2, Context) -> T, p1: P1, p2: P2): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1, p2).also { prepareComponent(it, STATE::class.java) }
    }
}

fun <P1, P2, P3, T> make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1, p2, p3)
    }
}

inline fun <P1, P2, P3, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1, p2, p3).also { prepareComponent(it, STATE::class.java) }
    }
}

fun <P1, P2, P3, P4, T> make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1, p2, p3, p4)
    }
}

inline fun <P1, P2, P3, P4, T: Component<STATE, *>, reified STATE> makeComponent(noinline factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): FactoryWithContext<T> {
    return { context: Context ->
        context.make(factory, p1, p2, p3, p4).also { prepareComponent(it, STATE::class.java) }
    }
}

fun <T> Context.make(factory: (Context) -> T): T {
    return make { -> factory(this) }
}

inline fun <T: Component<STATE, *>, reified STATE> Context.makeComponent(noinline factory: (Context) -> T): T {
    return makeComponent { -> factory(this) }
}

fun <P1, T> Context.make(factory: (P1, Context) -> T, p1: P1): T {
    return make { -> factory(p1, this) }
}

inline fun <P1, T: Component<STATE, *>, reified STATE> Context.makeComponent(noinline factory: (P1, Context) -> T, p1: P1): T {
    return makeComponent { factory(p1, this) }
}

fun <P1, P2, T> Context.make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return make { -> factory(p1, p2, this) }
}

inline fun <P1, P2, T: Component<STATE, *>, reified STATE> Context.makeComponent(noinline factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return makeComponent { -> factory(p1, p2, this) }
}

fun <P1, P2, P3, T> Context.make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
    return make { -> factory(p1, p2, p3, this) }
}

inline fun <P1, P2, P3, T: Component<STATE, *>, reified STATE> Context.makeComponent(noinline factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
    return makeComponent { -> factory(p1, p2, p3, this) }
}

fun <P1, P2, P3, P4, T> Context.make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return make { -> factory(p1, p2, p3, p4, this) }
}

inline fun <P1, P2, P3, P4, T: Component<STATE, *>, reified STATE> Context.makeComponent(noinline factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return makeComponent { -> factory(p1, p2, p3, p4, this) }
}

fun <T> View.make(factory: (Context) -> T): T {
    return context.make(factory)
}

inline fun <T: Component<STATE, *>, reified STATE> View.makeComponent(noinline factory: (Context) -> T): T {
    return context.makeComponent(factory)
}

fun <P1, T> View.make(factory: (P1, Context) -> T, p1: P1): T {
    return context.make(factory, p1)
}

inline fun <P1, T: Component<STATE, *>, reified STATE> View.makeComponent(noinline factory: (P1, Context) -> T, p1: P1): T {
    return context.makeComponent(factory, p1)
}

fun <P1, P2, T> View.make(factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return context.make(factory, p1, p2)
}

inline fun <P1, P2, T: Component<STATE, *>, reified STATE> View.makeComponent(noinline factory: (P1, P2, Context) -> T, p1: P1, p2: P2): T {
    return context.makeComponent(factory, p1, p2)
}

fun <P1, P2, P3, T> View.make(factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
    return context.make(factory, p1, p2, p3)
}

inline fun <P1, P2, P3, T: Component<STATE, *>, reified STATE> View.makeComponent(noinline factory: (P1, P2, P3, Context) -> T, p1: P1, p2: P2, p3: P3): T {
    return context.makeComponent(factory, p1, p2, p3)
}

fun <P1, P2, P3, P4, T> View.make(factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return context.make(factory, p1, p2, p3, p4)
}

inline fun <P1, P2, P3, P4, T: Component<STATE, *>, reified STATE> View.makeComponent(noinline factory: (P1, P2, P3, P4, Context) -> T, p1: P1, p2: P2, p3: P3, p4: P4): T {
    return context.makeComponent(factory, p1, p2, p3, p4)
}

fun <STATE> prepareComponent(component: Component<STATE, *>, stateClass: Class<STATE>) {
    Log.d("AAA", "AAA $component")
    if (stateClass == Unit::class) {
        Log.d("BBB", "BBB $component")
        @Suppress("UNCHECKED_CAST")
        component.componentState = Unit as STATE
    }
}
