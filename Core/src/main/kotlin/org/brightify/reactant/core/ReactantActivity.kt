package org.brightify.reactant.core

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ReactantActivity(private val wireframeFactory: (ReactantActivity) -> Wireframe) : AppCompatActivity() {

    private val onResumeSubject = PublishSubject.create<Unit>()

    val resumed: Observable<Unit>
        get() = onResumeSubject

    private val RootFragmentTag = "RootFragment"

    constructor(wireframe: Wireframe) : this({ wireframe })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, wireframeFactory(this).entryPoint().viewControllerWrapper, RootFragmentTag)
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

    override fun onResume() {
        super.onResume()
        onResumeSubject.onNext(Unit)
    }

    fun present(controller: ViewController, animated: Boolean = true) {
        val transaction = fragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        transaction.replace(android.R.id.content, controller.viewControllerWrapper)
        transaction.commit()
    }

    fun dismiss(animated: Boolean = true) {
        fragmentManager.popBackStackImmediate()
    }
}
