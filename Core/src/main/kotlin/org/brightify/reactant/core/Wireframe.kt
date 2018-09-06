package org.brightify.reactant.core

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.brightify.reactant.controller.NavigationController
import org.brightify.reactant.controller.ViewController
import java.lang.ref.WeakReference

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
abstract class Wireframe {

    abstract fun entrypoint(): ViewController
}

class FutureControllerProvider<T : ViewController> {

    internal var controllerRef: WeakReference<T>? = null

    val controller: T?
       get() = controllerRef?.get()

    val navigation: NavigationController?
        get() = controller?.navigationController
}

fun <T : ViewController> Wireframe.create(factory: (FutureControllerProvider<T>) -> T): T {
    val futureControllerProvider = FutureControllerProvider<T>()
    val controller = factory(futureControllerProvider)
    futureControllerProvider.controllerRef = WeakReference(controller)
    return controller
}

data class ControllerWithResult<T : ViewController, U>(val controller: T, val result: Observable<U>)

fun <T : ViewController, U> Wireframe.create(factory: (FutureControllerProvider<T>, Observer<U>) -> T): ControllerWithResult<T, U> {
    val futureControllerProvider = FutureControllerProvider<T>()
    val subject = PublishSubject.create<U>()
    val controller = factory(futureControllerProvider, subject)
    futureControllerProvider.controllerRef = WeakReference(controller)
    return ControllerWithResult(controller, subject)
}