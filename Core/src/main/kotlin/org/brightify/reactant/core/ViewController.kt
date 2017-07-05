package org.brightify.reactant.core

import android.view.View
import android.view.WindowManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.ReplaySubject
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewController {

    // TODO Error
    val activity: ReactantActivity
        get() = viewControllerWrapper.activity!!

    var navigationController: NavigationController? = null

    lateinit var contentView: View
        internal set

    internal lateinit var viewControllerWrapper: ViewControllerWrapper

    private val lifetimeDisposeBag = CompositeDisposable()

    var actionBarHidden: Boolean by Delegates.observable(false) { _, _, _ ->
        if (actionBarHidden) {
            activity.supportActionBar?.hide()
        } else {
            activity.supportActionBar?.show()
        }
    }

    var statusBarTranslucent: Boolean by Delegates.observable(false) { _, _, _ ->
        if (statusBarTranslucent) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

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
        actionBarHidden = actionBarHidden
        statusBarTranslucent = statusBarTranslucent
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

    fun <C : ViewController> present(controller: Observable<C>, animated: Boolean = true): Observable<C> {
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
                .addTo(lifetimeDisposeBag)
        return replay
    }
}
