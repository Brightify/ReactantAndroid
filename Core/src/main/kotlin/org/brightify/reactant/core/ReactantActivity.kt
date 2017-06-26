package org.brightify.reactant.core

import android.os.Build
import android.support.v7.app.AppCompatActivity
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ReactantActivity : AppCompatActivity() {

    var rootViewController: ViewController? by object : ObservableProperty<ViewController?>(null) {

        override fun afterChange(property: KProperty<*>, oldValue: ViewController?, newValue: ViewController?) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, rootViewController)
            transaction.commit()
        }
    }

    override fun onBackPressed() {
        if ((fragmentManager.top ?: rootViewController)?.onBackPressed() != true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else {
                finish()
            }
        }
    }

    fun present(controller: ViewController, animated: Boolean = true) {
        val transaction = fragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.replace(android.R.id.content, controller)
        transaction.commit()
    }

    fun dismiss(animated: Boolean = true) {
        fragmentManager.popBackStackImmediate()
    }
}
