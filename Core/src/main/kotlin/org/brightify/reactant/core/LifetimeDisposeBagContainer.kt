package org.brightify.reactant.core

import android.util.Log
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.Collections
import java.util.WeakHashMap

/**
 * Created by TadeasKriz on 8/25/17.
 */

interface LifetimeDisposeBagContainer {
    val lifetimeDisposeBag: CompositeDisposable

    val onDispose: Observable<Unit>

    fun retain()

    fun release()

    fun addChildContainer(container: LifetimeDisposeBagContainer)

    fun removeChildContainer(container: LifetimeDisposeBagContainer)
}

interface LifetimeDisposeBagContainerWithDelegate: LifetimeDisposeBagContainer {
    val lifetimeDisposeBagContainerDelegate: LifetimeDisposeBagContainerDelegate

    override val lifetimeDisposeBag: CompositeDisposable
        get() = lifetimeDisposeBagContainerDelegate.lifetimeDisposeBag

    override val onDispose: Observable<Unit>
        get() = lifetimeDisposeBagContainerDelegate.onDispose

    override fun retain() {
        Log.d("Lifetime", "Retaining: $this")
        lifetimeDisposeBagContainerDelegate.retain()
    }

    override fun release() {
        Log.d("Lifetime", "Releasing: $this")
        lifetimeDisposeBagContainerDelegate.release()
    }

    override fun addChildContainer(container: LifetimeDisposeBagContainer) {
        Log.d("Lifetime", "Adding child: $container (this: $this)")
        lifetimeDisposeBagContainerDelegate.addChildContainer(container)
    }

    override fun removeChildContainer(container: LifetimeDisposeBagContainer) {
        Log.d("Lifetime", "Removing child: $container (this: $this)")
        lifetimeDisposeBagContainerDelegate.removeChildContainer(container)
    }
}

class LifetimeDisposeBagContainerDelegate(private val onFirstRetain: () -> Unit): LifetimeDisposeBagContainer {

    override val onDispose: Observable<Unit>
        get() = disposeSubject

    override val lifetimeDisposeBag: CompositeDisposable
        get() {
            if (retainCount == null) {
                throw IllegalStateException("lifetimeDisposeBag accessed before first retain, that's illegal!")
            }
            return disposeBag
        }

    private val disposeBag = CompositeDisposable()
    private val disposeSubject = PublishSubject.create<Unit>()
    private val childContainers: MutableSet<LifetimeDisposeBagContainer> = Collections.newSetFromMap(WeakHashMap())
    private val lock = Any()
    private var retainCount: Int? = null

    override fun retain() {
        synchronized(lock) {
            val count = retainCount
            retainCount = when {
                count == null -> { 1 }
                count == 0 -> throw IllegalStateException("Trying to retain a disposed container, that's illegal!")
                count > 0 -> count + 1
                else -> throw IllegalStateException("Retain count was $retainCount and retain was called. This is an undefined state!")
            }
            if (count == null) {
                onFirstRetain()
            }
        }
    }

    override fun release() {
        synchronized(lock) {
            val count = retainCount
            retainCount = when {
                count == null -> throw IllegalStateException("Trying to release a container that was never retained!")
                count == 1 -> {
                    for (child in childContainers) {
                        child.release()
                    }
                    childContainers.clear()
                    disposeSubject.onNext(Unit)
                    lifetimeDisposeBag.dispose()
                    0
                }
                count > 0 -> count - 1
                count == 0 -> throw IllegalStateException("Trying to release a ")
                else -> throw IllegalStateException("Retain count was $retainCount and release was called. This is an undefined state!")
            }
        }
    }

    override fun addChildContainer(container: LifetimeDisposeBagContainer) {
        synchronized(lock) {
            if (!childContainers.contains(container)) {
                container.retain()
                childContainers.add(container)
            }
        }
    }

    override fun removeChildContainer(container: LifetimeDisposeBagContainer) {
        synchronized(lock) {
            if (childContainers.contains(container)) {
                childContainers.remove(container)
                container.release()
            }
        }
    }
}
