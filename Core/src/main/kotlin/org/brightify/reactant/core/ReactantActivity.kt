package org.brightify.reactant.core

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import org.brightify.reactant.controller.ViewController
import java.util.Stack

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
// TODO ViewControllerActivity
// TODO resources to ViewController and ViewBase
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

    private val onResumeSubject = PublishSubject.create<Unit>()

    val resumed: Observable<Unit>
        get() = onResumeSubject

    private val contentView: FrameLayout
        get() = findViewById(android.R.id.content) as FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this

        if (savedInstanceState == null) {
            viewControllerStack.clear()
        }

        if (viewControllerStack.empty()) {
            viewControllerStack.push(wireframeFactory().entryPoint())
            viewControllerStack.peek().loadViewIfNeeded()
        }
    }

    override fun onBackPressed() {
        if (!viewControllerStack.peek().onBackPressed()) {
            dismissOrFinish()
        }
    }

    override fun onStart() {
        super.onStart()

        viewControllerStack.forEach {
            it.viewWillAppear()
            contentView.addView(it.view)
        }
    }

    override fun onResume() {
        super.onResume()

        viewControllerStack.forEach { it.viewDidAppear() }
        onResumeSubject.onNext(Unit)
    }

    override fun onPause() {
        super.onPause()

        viewControllerStack.forEach { it.viewWillDisappear() }
    }

    override fun onStop() {
        super.onStop()

        contentView.removeAllViews()
        viewControllerStack.forEach { it.viewDidDisappear() }
    }

    fun present(viewController: ViewController, animated: Boolean = true): Observable<Unit> {
        viewController.loadViewIfNeeded()
        viewControllerStack.push(viewController)
        viewController.viewWillAppear()
        contentView.addView(viewController.view)
        viewController.viewDidAppear()
        return Observable.just(Unit)
    }

    fun dismiss(animated: Boolean = true) {
        dismissOrFinish()
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

    private fun dismissOrFinish() {
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
