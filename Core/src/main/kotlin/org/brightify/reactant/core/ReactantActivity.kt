package org.brightify.reactant.core

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import org.brightify.reactant.controller.ViewController
import org.brightify.reactant.controller.util.TransactionManager
import java.util.Stack

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ReactantActivity(private val wireframeFactory: () -> Wireframe): AppCompatActivity(), LifetimeDisposeBagContainerWithDelegate {

    companion object {

        val instance: ReactantActivity
            get() = instanceOrNull ?: throw IllegalStateException("Instance has not been created yes.")

        var instanceOrNull: ReactantActivity? = null
            private set

        private val viewControllerStack = Stack<ViewController>()

        val context: Context by lazy {
            instance.applicationContext.setTheme(instance.applicationInfo.theme)
            instance.applicationContext
        }
    }

    override val lifetimeDisposeBagContainerDelegate = LifetimeDisposeBagContainerDelegate({ /* No action on first retain */ })

    private val transactionManager = TransactionManager()

    private val onResumeSubject = PublishSubject.create<Unit>()
    private val onPauseSubject = PublishSubject.create<Unit>()
    private val onDestroySubject = PublishSubject.create<Unit>()

    val resumed: Observable<Unit>
        get() = onResumeSubject

    val paused: Observable<Unit>
        get() = onPauseSubject

    val destroyed: Observable<Unit>
        get() = onDestroySubject

    val beforeKeyboardVisibilityChanged: Observable<Boolean>
        get() = contentView.beforeKeyboardVisibilityChangeSubject

    val afterKeyboardVisibilityChanged: Observable<Boolean>
        get() = beforeKeyboardVisibilityChanged.flatMap { value -> onLayoutSubject.take(1).map { value } }

    var screenOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    private lateinit var contentView: ReactantActivityContentView

    private val onLayoutSubject = PublishSubject.create<Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instanceOrNull = this

        retain()

        contentView = ReactantActivityContentView(this)
        setContentView(contentView)

        transactionManager.transaction {
            if (savedInstanceState == null) {
                viewControllerStack.clear()
            }

            if (viewControllerStack.empty()) {
                viewControllerStack.push(wireframeFactory().entryPoint())
                viewControllerStack.peek().retain()
                viewControllerStack.peek().loadViewIfNeeded()
            }
        }

        contentView.viewTreeObserver.addOnGlobalLayoutListener {
            onLayoutSubject.onNext(Unit)
        }

        transactionManager.enabled = true
    }

    override fun onBackPressed() {
        if (!viewControllerStack.peek().onBackPressed()) {
            dismissOrFinish()
        }
    }

    override fun onStart() {
        super.onStart()

        transactionManager.transaction {
            viewControllerStack.lastOrNull()?.let {
                it.viewWillAppear()
                contentView.addView(it.view)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        transactionManager.transaction {
            viewControllerStack.lastOrNull()?.viewDidAppear()
        }
        onResumeSubject.onNext(Unit)
    }

    override fun onPause() {
        super.onPause()

        transactionManager.transaction {
            viewControllerStack.lastOrNull()?.viewWillDisappear()
        }
        onPauseSubject.onNext(Unit)
    }

    override fun onStop() {
        super.onStop()

        contentView.removeAllViews()
        transactionManager.transaction {
            viewControllerStack.lastOrNull()?.viewDidDisappear()
        }
    }

    override fun onDestroy() {
        onDestroySubject.onNext(Unit)
        release()
        instanceOrNull = null

        super.onDestroy()
    }

    fun present(viewController: ViewController, animated: Boolean = true): Observable<Unit> {
        transactionManager.transaction {
            viewControllerStack.lastOrNull()?.let {
                it.viewWillDisappear()
                contentView.removeView(it.view)
                it.viewDidDisappear()
            }
            viewController.retain()
            viewController.loadViewIfNeeded()
            viewControllerStack.push(viewController)
            viewController.viewWillAppear()
            contentView.addView(viewController.view)
            viewController.viewDidAppear()
        }
        return Observable.just(Unit)
    }

    fun dismiss(animated: Boolean = true): Observable<Unit> {
        dismissOrFinish()
        return Observable.just(Unit)
    }

    fun <C: ViewController> present(viewController: Observable<C>, animated: Boolean = true): Observable<C> {
        val replay = ReplaySubject.create<C>(1)
        viewController
                .switchMap { controllerInstance ->
                    present(viewController = controllerInstance).map { controllerInstance } ?: Observable.empty<C>()
                }
                .subscribeBy(onNext = {
                    replay.onNext(it)
                }, onComplete = {
                    replay.onComplete()
                })
                .addTo(lifetimeDisposeBag)
        return replay
    }

    fun invalidateChildren() {
        transactionManager.transaction {
            contentView.removeAllViews()
            viewControllerStack.lastOrNull()?.let { contentView.addView(it.view) }
        }
    }

    fun updateScreenOrientation() {
        requestedOrientation = screenOrientation
    }

    private fun dismissOrFinish() {
        transactionManager.transaction {
            if (viewControllerStack.size > 1) {
                viewControllerStack.peek().viewWillDisappear()
                contentView.removeView(viewControllerStack.peek().view)
                viewControllerStack.peek().viewDidDisappear()
                viewControllerStack.pop().release()
                viewControllerStack.peek().viewWillAppear()
                contentView.addView(viewControllerStack.peek().view)
                viewControllerStack.peek().viewDidAppear()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition()
                } else {
                    finish()
                }
            }
        }
    }
}
