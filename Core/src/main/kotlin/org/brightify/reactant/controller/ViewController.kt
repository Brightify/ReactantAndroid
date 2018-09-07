package org.brightify.reactant.controller

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.util.onChange
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
open class ViewController(title: String = "") {

    val visibleDisposeBag = CompositeDisposable()

    val viewLifetimeDisposeBag = CompositeDisposable()

    val activeDisposeBag = CompositeDisposable()

    var activity: ReactantActivity
        get() = activity_ ?: throw IllegalStateException("activity cannot be accessed when controller is not activated.")
        set(value) {
            activity_ = value
        }

    var view: View
        get() = view_ ?: viewNotLoadedError()
        set(value) {
            view_ = value
        }

    var navigationController: NavigationController? by onChange<NavigationController?>(null) { _, _, _ ->
        if (tabBarItem != null) {
            navigationController?.tabBarItem = tabBarItem
        }
        if (hamburgerMenuItem != null) {
            navigationController?.hamburgerMenuItem = hamburgerMenuItem
        }
    }
        internal set

    var tabBarController: TabBarController? by onChange<TabBarController?>(null) { _, _, _ ->
        if (tabBarItem != null) {
            tabBarController?.updateTabBarItem(this)
        }
        if (hamburgerMenuItem != null) {
            tabBarController?.hamburgerMenuItem = hamburgerMenuItem
        }
    }
        internal set

    var hamburgerMenuController: HamburgerMenuController? by onChange<HamburgerMenuController?>(null) { _, _, _ ->
        if (tabBarItem != null) {
            hamburgerMenuController?.tabBarItem = tabBarItem
        }
        if (hamburgerMenuItem != null) {
            hamburgerMenuController?.updateMenuItem(this)
        }
    }
        internal set

    var title: String by Delegates.observable(title) { _, _, newValue ->
        activity.title = newValue
    }

    open var hidesBottomBarWhenPushed: Boolean = false

    var tabBarItem: MenuItem? by onChange<MenuItem?>(null) { _, _, _ ->
        navigationController?.tabBarItem = tabBarItem
        tabBarController?.updateTabBarItem(this)
        hamburgerMenuController?.tabBarItem = tabBarItem
    }

    var hamburgerMenuItem: MenuItem? by onChange<MenuItem?>(null) { _, _, _ ->
        navigationController?.hamburgerMenuItem = hamburgerMenuItem
        tabBarController?.hamburgerMenuItem = hamburgerMenuItem
        hamburgerMenuController?.updateMenuItem(this)
    }

    var statusBarColor: Int
        get() = activity.window.statusBarColor
        set(value) {
            lastStatusBarColor = value
            activity.window.statusBarColor = value
        }

    var screenOrientation: Int
        get() = activity.screenOrientation
        set(value) {
            lastScreenOrientation = value
            activity.screenOrientation = value
            activity.updateScreenOrientation()
        }

    var isVisible = false
        private set

    val isViewLoaded: Boolean
        get() = view_ != null

    val isActivated: Boolean
        get() = activity_ != null

    private var initialized = false

    private var lastStatusBarColor: Int? = null
    private var lastScreenOrientation: Int? = null

    internal var activity_: ReactantActivity? by object: ObservableProperty<ReactantActivity?>(null) {

        override fun beforeChange(property: KProperty<*>, oldValue: ReactantActivity?, newValue: ReactantActivity?): Boolean {
            if (oldValue == newValue) {
                return false
            }

            view_ = null
            if (isActivated && newValue == null) {
                deactivated()
            }
            return true
        }

        override fun afterChange(property: KProperty<*>, oldValue: ReactantActivity?, newValue: ReactantActivity?) {
            if (oldValue == null && newValue != null) {
                activated()
            }
            activityChanged()
        }
    }

    internal var view_: View? by object: ObservableProperty<View?>(null) {

        override fun beforeChange(property: KProperty<*>, oldValue: View?, newValue: View?): Boolean {
            if (oldValue == newValue) {
                return false
            }

            if (isViewLoaded && newValue == null) {
                if (isVisible) {
                    viewWillDisappear()
                    (view.parent as? ViewGroup)?.removeView(view)
                    viewDidDisappear()
                }
                viewDestroyed()
            }
            return true
        }

        override fun afterChange(property: KProperty<*>, oldValue: View?, newValue: View?) {
            if (view_ != null) {
                if (isVisible) {
                    when {
                        navigationController != null -> navigationController?.invalidateChild()
                        tabBarController != null -> tabBarController?.invalidateChild()
                        hamburgerMenuController != null -> hamburgerMenuController?.invalidateChild()
                        else -> activity.invalidateChildren()
                    }
                }
                view_?.isClickable = true
            }
        }
    }

    open fun activated() {
        activeDisposeBag.clear()
    }

    open fun activityChanged() {
    }

    open fun loadView() {
        viewLifetimeDisposeBag.clear()
    }

    open fun viewDidLoad() {
    }

    open fun viewWillAppear() {
        if (view_ == null) {
            loadView()
            viewDidLoad()
        }

        visibleDisposeBag.clear()
        title = title

        if (this !is NavigationController && this !is TabBarController && this !is HamburgerMenuController) {
            invalidateGlobalSettings()
            activity.updateScreenOrientation()
        }
    }

    open fun viewDidAppear() {
        isVisible = true
    }

    open fun viewWillDisappear() {
        activity.let {
            val inputManager = it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    open fun viewDidDisappear() {
        isVisible = false
        visibleDisposeBag.clear()
    }

    open fun viewDestroyed() {
        viewLifetimeDisposeBag.clear()
    }

    open fun deactivated() {
        activeDisposeBag.clear()
    }

    /**
     * Returns true if event is handled.
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    open fun destroyViewHierarchy() {
        view_ = null
    }

    fun present(controller: ViewController, animated: Boolean = true): Observable<Unit> {
        return activity.present(controller, animated)
    }

    fun dismiss(animated: Boolean = true): Observable<Unit> {
        return activity.dismiss(animated)
    }

    fun <C: ViewController> present(controller: Observable<C>, animated: Boolean = true): Observable<C> {
        return activity.present(controller, animated)
    }

    fun resetRememberedStatusBarColor() {
        lastStatusBarColor = null
    }

    internal fun invalidateGlobalSettings() {
        navigationController?.invalidateGlobalSettings()
        tabBarController?.invalidateGlobalSettings()
        hamburgerMenuController?.invalidateGlobalSettings()
        lastStatusBarColor?.let { statusBarColor = it }
        lastScreenOrientation?.let { activity.screenOrientation = it }
    }

    protected fun viewNotLoadedError(): Nothing {
        throw IllegalStateException("view cannot be accessed before it is loaded.")
    }
}
