package org.brightify.reactant.core

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.PublishSubject
import org.brightify.reactant.core.controller.NavigationController
import org.brightify.reactant.core.controller.ViewController

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
abstract class Wireframe {

    abstract fun entryPoint(): ViewController
}

class FutureControllerProvider<T : ViewController> {

    var controller: T? = null

    val navigation: NavigationController?
        get() = controller?.navigationController
}

fun <T : ViewController> Wireframe.create(factory: (FutureControllerProvider<T>) -> T): T {
    val futureControllerProvider = FutureControllerProvider<T>()
    val controller = factory(futureControllerProvider)
    futureControllerProvider.controller = controller
    return controller
}

data class ControllerWithResult<T : ViewController, U>(val controller: T, val result: Observable<U>)

fun <T : ViewController, U> Wireframe.create(factory: (FutureControllerProvider<T>, Observer<U>) -> T): ControllerWithResult<T, U> {
    val futureControllerProvider = FutureControllerProvider<T>()
    val subject: PublishSubject<U> = PublishSubject.create()
    val controller = factory(futureControllerProvider, subject)
    futureControllerProvider.controller = controller
    return ControllerWithResult(controller, subject)
}