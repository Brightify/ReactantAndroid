package org.brightify.reactant.rx

import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.ReplaySubject
import org.brightify.reactant.controller.NavigationController
import org.brightify.reactant.controller.ViewController
import org.brightify.reactant.core.ControllerWithResult

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
fun <C : ViewController> NavigationController.push(viewController: Observable<C>, animated: Boolean = true) {
    viewController.subscribe { push(it, animated) }.addTo(lifetimeDisposeBag)
}

fun <C : ViewController> NavigationController.replace(viewController: Observable<C>,
                                                      animated: Boolean = true): Observable<Optional<ViewController>> {
    val replacedController = ReplaySubject.create<Optional<ViewController>>(1)
    viewController
            .subscribeBy(
                    onNext = { controller ->
                        replacedController.onNext(replace(viewController = controller, animated = animated).toOptional())
                    },
                    onComplete = { replacedController.onComplete() }
            )
            .addTo(lifetimeDisposeBag)
    return replacedController
}

fun <C : ViewController> NavigationController.popAllAndReplace(viewController: Observable<C>): Observable<List<ViewController>> {
    return replaceAll(viewController, false)
}

fun <C : ViewController> NavigationController.replaceAll(viewController: Observable<C>,
                                                         animated: Boolean = true): Observable<List<ViewController>> {
    val oldControllers = ReplaySubject.create<List<ViewController>>(1)
    viewController
            .subscribeBy(
                    onNext = { oldControllers.onNext(replaceAll(viewController = it, animated = animated)) },
                    onComplete = { oldControllers.onComplete() }
            )
            .addTo(lifetimeDisposeBag)

    return oldControllers
}

fun <C : ViewController, T> NavigationController.push(viewController: Observable<ControllerWithResult<C, T>>,
                                                      animated: Boolean = true): Observable<T> {
    val sharedController = viewController.replay(1).refCount()

    sharedController.map { it.controller }
            .subscribeBy(
                    onNext = { push(viewController = it, animated = animated) }
            )
            .addTo(lifetimeDisposeBag)

    return sharedController.flatMap { it.result }
}
