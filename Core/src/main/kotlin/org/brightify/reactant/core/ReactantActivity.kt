package org.brightify.reactant.core

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ReactantActivity(private val wireframeFactory: (ReactantActivity) -> Wireframe) : AppCompatActivity() {

    private val RootFragmentTag = "RootFragment"

    constructor(wireframe: Wireframe) : this({ wireframe })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, ViewControllerWrapper(wireframeFactory(this).entryPoint()), RootFragmentTag)
            transaction.commit()
        }
    }

    override fun onBackPressed() {
        if ((fragmentManager.top ?: fragmentManager.findFragmentByTag(
                RootFragmentTag) as? ViewControllerWrapper)?.viewController?.onBackPressed() != true) {
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
        transaction.replace(android.R.id.content, ViewControllerWrapper(controller))
        transaction.commit()
    }

    fun dismiss(animated: Boolean = true) {
        fragmentManager.popBackStackImmediate()
    }
}
