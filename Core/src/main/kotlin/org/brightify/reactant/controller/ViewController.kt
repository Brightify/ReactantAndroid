package org.brightify.reactant.controller

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer
import org.brightify.reactant.core.LifetimeDisposeBagContainerDelegate
import org.brightify.reactant.core.LifetimeDisposeBagContainerWithDelegate
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.util.onChange
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewController(title: String = ""): LifetimeDisposeBagContainerWithDelegate {
    val visibleDisposeBag = CompositeDisposable()

    override val lifetimeDisposeBagContainerDelegate = LifetimeDisposeBagContainerDelegate { init() }

    var view: View by onChange(View(ReactantActivity.context)) { _, _, _ ->
        if (isVisible) {
            if (navigationController != null) {
                navigationController?.invalidateChild()
            } else if (tabBarController != null) {
                tabBarController?.invalidateChild()
            } else {
                ReactantActivity.instance.invalidateChildren()
            }
        }
        view.isClickable = true
    }

    var navigationController: NavigationController? by onChange<NavigationController?>(null) { _, _, _ ->
            if (tabBarItem != null) {
                navigationController?.tabBarItem = tabBarItem
            }
        }
        internal set

    var tabBarController: TabBarController? by onChange<TabBarController?>(null) { _, _, _ ->
            if (tabBarItem != null) {
                tabBarController?.updateTabBarItem(this)
            }
        }
        internal set

    var title: String by Delegates.observable(title) { _, _, newValue ->
        ReactantActivity.instance.title = newValue
    }

    open var hidesBottomBarWhenPushed: Boolean = false

    var tabBarItem: TabBarItem? by onChange<TabBarItem?>(null) { _, _, _ ->
        navigationController?.tabBarItem = tabBarItem
        tabBarController?.updateTabBarItem(this)
    }

    var statusBarColor: Int
        get() = ReactantActivity.instance.window.statusBarColor
        set(value) {
            lastStatusBarColor = value
            ReactantActivity.instance.window.statusBarColor = value
        }

    private var loaded = false
    private var isVisible = false
    private var lastStatusBarColor: Int? = null

    init {
        view.isClickable = true
    }

    internal open fun init() { }

    open fun loadView() {
    }

    open fun viewDidLoad() {
    }

    open fun viewWillAppear() {
        visibleDisposeBag.clear()
        title = title
        invalidateStatusBarColor()
    }

    open fun viewDidAppear() {
        isVisible = true
    }

    open fun viewWillDisappear() {
        ReactantActivity.instance.let {
            val inputManager = it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    open fun viewDidDisappear() {
        isVisible = false
        visibleDisposeBag.clear()
    }

    /**
     * Returns true if event is handled.
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    fun loadViewIfNeeded() {
        if (!loaded) {
            loadView()
            viewDidLoad()
            loaded = true
        }
    }

    fun present(controller: ViewController, animated: Boolean = true): Observable<Unit> {
        return ReactantActivity.instance.present(controller, animated)
    }

    fun dismiss(animated: Boolean = true): Observable<Unit> {
        return ReactantActivity.instance.dismiss(animated)
    }

    fun <C : ViewController> present(controller: Observable<C>, animated: Boolean = true): Observable<C> {
        return ReactantActivity.instance.present(controller, animated)
    }

    fun invalidateStatusBarColor() {
        navigationController?.invalidateStatusBarColor()
        tabBarController?.invalidateStatusBarColor()
        lastStatusBarColor?.let { statusBarColor = it }
    }

    fun resetRememberedStatusBarColor() {
        lastStatusBarColor = null
    }
}
