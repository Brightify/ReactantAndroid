package org.brightify.reactant.core

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
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
open class ReactantActivity(private val wireframeFactory: () -> Wireframe) : AppCompatActivity() {

    companion object {

        lateinit var instance: ReactantActivity

        private val viewControllerStack = Stack<ViewController>()

        val context: Context by lazy {
            instance.applicationContext.setTheme(instance.applicationInfo.theme)
            instance.applicationContext
        }
    }

    private val lifetimeDisposeBag = CompositeDisposable()

    private val transactionManager = TransactionManager()

    private val onResumeSubject = PublishSubject.create<Unit>()
    private val onPauseSubject = PublishSubject.create<Unit>()

    val resumed: Observable<Unit>
        get() = onResumeSubject

    val paused: Observable<Unit>
        get() = onPauseSubject

    val keyboardVisibilityChanged: Observable<Boolean>
        get() = contentView.keyboardVisibilityChangeSubject

    private lateinit var contentView: ReactantActivityContentView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this

        contentView = ReactantActivityContentView(this)
        setContentView(contentView)

        transactionManager.transaction {
            if (savedInstanceState == null) {
                viewControllerStack.clear()
            }

            if (viewControllerStack.empty()) {
                viewControllerStack.push(wireframeFactory().entryPoint())
                viewControllerStack.peek().loadViewIfNeeded()
            }
        }
    }

    override fun onBackPressed() {
        if (!viewControllerStack.peek().onBackPressed()) {
            dismissOrFinish()
        }
    }

    override fun onStart() {
        super.onStart()

        transactionManager.transaction {
            viewControllerStack.forEach {
                it.viewWillAppear()
                contentView.addView(it.view)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        transactionManager.transaction {
            viewControllerStack.forEach { it.viewDidAppear() }
        }
        onResumeSubject.onNext(Unit)
    }

    override fun onPause() {
        super.onPause()

        transactionManager.transaction {
            viewControllerStack.forEach { it.viewWillDisappear() }
        }
        onPauseSubject.onNext(Unit)
    }

    override fun onStop() {
        super.onStop()

        contentView.removeAllViews()
        transactionManager.transaction {
            viewControllerStack.forEach { it.viewDidDisappear() }
        }
    }

    fun present(viewController: ViewController, animated: Boolean = true): Observable<Unit> {
        transactionManager.transaction {
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

    fun <C : ViewController> present(viewController: Observable<C>, animated: Boolean = true): Observable<C> {
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
            viewControllerStack.forEach { contentView.addView(it.view) }
        }
    }

    private fun dismissOrFinish() {
        transactionManager.transaction {
            if (viewControllerStack.size > 1) {
                viewControllerStack.peek().viewWillDisappear()
                contentView.removeView(viewControllerStack.peek().view)
                viewControllerStack.peek().viewDidDisappear()
                viewControllerStack.pop()
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
