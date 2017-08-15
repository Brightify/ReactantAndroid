package org.brightify.reactant.core

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.FrameLayout
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

/**
*  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
*/
class ReactantActivityContentView(context: Context): FrameLayout(context) {

    val keyboardVisibilityChangeSubject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()

    private var keyboardState = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        val decorView = ReactantActivity.instance.window.decorView
        val windowHeight = decorView.height
        val statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val statusBar = decorView.findViewById(android.R.id.statusBarBackground)
            if (statusBar != null && statusBar.visibility == View.VISIBLE) {
                statusBar.height
            } else {
                0
            }
        } else {
            0
        }
        val navigationBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val navigationBar = decorView.findViewById(android.R.id.navigationBarBackground)
            if (navigationBar != null && navigationBar.visibility == View.VISIBLE) {
                navigationBar.height
            } else {
                0
            }
        } else {
            0
        }

        if (windowHeight != 0) {
            val newState = windowHeight - statusBarHeight - navigationBarHeight > measuredHeight
            if (keyboardState != newState) {
                keyboardVisibilityChangeSubject.onNext(newState)
                keyboardState = newState
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
