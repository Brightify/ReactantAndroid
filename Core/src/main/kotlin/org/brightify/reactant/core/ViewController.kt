package org.brightify.reactant.core

import android.view.View
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.ReplaySubject

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewController {

    val activity: ReactantActivity
        get() = viewControllerWrapper.activity!!

    var navigationController: NavigationController? = null

    lateinit var contentView: View
        internal set

    internal lateinit var viewControllerWrapper: ViewControllerWrapper

    private val lifetimeDisposeBag = CompositeDisposable()

    /**
     * Returns true if event is handled.
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    open fun onCreate() {
        contentView = View(activity)
    }

    open fun onActivityCreated() {
    }

    open fun onStart() {
    }

    open fun onResume() {
    }

    open fun onPause() {
    }

    open fun onStop() {
    }

    fun present(controller: ViewController, animated: Boolean = true): Observable<Unit> {
        activity.present(controller, animated)
        return Observable.just(Unit)
    }

    fun dismiss(animated: Boolean = true) {
        activity.dismiss(animated)
    }

    fun <C: ViewController>present(controller: Observable<C>, animated: Boolean = true): Observable<C> {
        val replay = ReplaySubject.create<C>(1)
        controller
                .switchMap { controllerInstance ->
                    present(controller = controllerInstance).map { controllerInstance } ?: Observable.empty<C>()
                }
                .subscribeBy(onNext = {
                    replay.onNext(it)
                }, onComplete = {
                    replay.onComplete()
                })
                .addTo(lifetimeDisposeBag )
        return replay
    }
}
