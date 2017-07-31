package org.brightify.reactant.core

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.brightify.reactant.core.util.push
import org.brightify.reactant.core.util.pushModal

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
open class ReactantActivity(private val wireframeFactory: (ReactantActivity) -> Wireframe) : AppCompatActivity() {

    private val onResumeSubject = PublishSubject.create<Unit>()

    val resumed: Observable<Unit>
        get() = onResumeSubject

    constructor(wireframe: Wireframe) : this({ wireframe })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val transaction = fragmentManager.beginTransaction()
            transaction.push(android.R.id.content, wireframeFactory(this).entryPoint().viewControllerWrapper)
            transaction.commit()
        }
    }

    override fun onBackPressed() {
        if (fragmentManager.top?.viewController?.onBackPressed() != true) {
            dismissOrFinish()
        }
    }

    override fun onResume() {
        super.onResume()
        onResumeSubject.onNext(Unit)
    }

    fun present(controller: ViewController, animated: Boolean = true) {
        val transaction = fragmentManager.beginTransaction()
        transaction.pushModal(android.R.id.content, controller.viewControllerWrapper)
        transaction.commit()
    }

    fun dismiss(animated: Boolean = true) {
        dismissOrFinish()
    }

    private fun dismissOrFinish() {
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else {
                finish()
            }
        }
    }
}
