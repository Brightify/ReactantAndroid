package org.brightify.reactant.core

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import io.reactivex.subjects.PublishSubject
import org.brightify.reactant.R

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
@SuppressLint("ViewConstructor")
class ReactantActivityContentView(private val activity: ReactantActivity): FrameLayout(activity) {

    val beforeKeyboardVisibilityChangeSubject: PublishSubject<Boolean> = PublishSubject.create()

    private var keyboardState = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        val decorView = activity.window.decorView
        val windowHeight = decorView.height
        val statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBar = decorView.findViewById<View>(android.R.id.statusBarBackground)
            if (statusBar != null && statusBar.visibility == View.VISIBLE) {
                statusBar.height
            } else {
                0
            }
        } else {
            0
        }
        val navigationBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val navigationBar = decorView.findViewById<View>(android.R.id.navigationBarBackground)
            if (navigationBar != null && navigationBar.visibility == View.VISIBLE) {
                navigationBar.height
            } else {
                0
            }
        } else {
            0
        }
        val actionBarHeight = decorView.findViewById<View>(R.id.action_bar)?.let {
            if (it.visibility == View.VISIBLE) it.height else 0
        } ?: 0

        if (windowHeight != 0) {
            val newState = windowHeight - statusBarHeight - navigationBarHeight - actionBarHeight > measuredHeight
            if (keyboardState != newState) {
                beforeKeyboardVisibilityChangeSubject.onNext(newState)
                keyboardState = newState
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
