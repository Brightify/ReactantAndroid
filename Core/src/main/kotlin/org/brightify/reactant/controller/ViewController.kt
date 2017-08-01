package org.brightify.reactant.controller

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowManager
import io.reactivex.Observable
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.util.onChange
import kotlin.properties.Delegates

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ViewController(title: String = "") {

    lateinit var view: View // TODO onChange fix for navigation controller

    var navigationController: NavigationController? = null
        internal set

    var tabBarController: TabBarController? = null
        internal set

    var title: String by Delegates.observable(title) { _, _, newValue ->
        ReactantActivity.instance.title = newValue
    }

    open var hidesBottomBarWhenPushed: Boolean = false

    open var tabBarItem: TabBarItem? by onChange<TabBarItem?>(null) { _, _, _ ->
        // TODO
        tabBarController?.updateTabBarItems()
    }

    var statusBarTranslucent: Boolean by Delegates.observable(false) { _, _, _ ->
        if (statusBarTranslucent) {
            ReactantActivity.instance.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            ReactantActivity.instance.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    var statusBarColor: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return ReactantActivity.instance.window.statusBarColor
            }
            Log.w("Reactant", "You are attempting to use status bar color API on unsupported OS version")
            return Color.BLACK
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ReactantActivity.instance.window.statusBarColor = value
            } else {
                Log.w("Reactant", "You are attempting to use status bar color API on unsupported OS version")
            }
        }

    private var loaded = false

    open fun loadView() {
    }

    open fun viewDidLoad() {
    }

    open fun viewWillAppear() {
        title = title
    }

    open fun viewDidAppear() {
    }

    open fun viewWillDisappear() {
    }

    open fun viewDidDisappear() {
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

    fun dismiss(animated: Boolean = true) {
        ReactantActivity.instance.dismiss(animated)
    }

    fun <C : ViewController> present(controller: Observable<C>, animated: Boolean = true): Observable<C> {
        return ReactantActivity.instance.present(controller, animated)
    }
}
