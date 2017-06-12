package org.brightify.reactant.prototyping

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.brightify.reactant.core.Button
import org.brightify.reactant.core.TextView
import org.brightify.reactant.core.ViewBase
import org.brightify.reactant.core.children
import org.brightify.reactant.core.component.setComponentState
import java.util.concurrent.TimeUnit

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = CustomView("Title", this)
        view.init()
        setContentView(view)

        Observable.interval(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            view.setComponentState((it.toInt() % 15 + 1) * 0x110000)
        }

        view.action.subscribe {
            view.setComponentState(0x110000)
        }
    }
}

class CustomView(title: String, context: Context) : ViewBase<Int, Unit>(context) {

    private val titleView = make(::TextView, title)
    private val button = make(::Button, "Press to reset")

    override val actions: List<Observable<Unit>> = listOf(button.clicks())

    override fun loadView() {
        children(
                titleView,
                button
        )
    }

    override fun setupConstraints() {

    }

    override fun update() {
        setBackgroundColor(Color.parseColor("#" + Integer.toHexString(componentState)))
    }
}
