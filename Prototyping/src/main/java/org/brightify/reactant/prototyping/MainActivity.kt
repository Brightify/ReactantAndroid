package org.brightify.reactant.prototyping

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import org.brightify.reactant.core.Button
import org.brightify.reactant.core.TextView
import org.brightify.reactant.core.ViewBase
import org.brightify.reactant.core.component.setComponentState
import org.brightify.reactant.core.constraint.snp

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = CustomView("Title", this)
        view.init()
        setContentView(view)

        view.action.subscribe {
            view.setComponentState(0x110000)
        }
    }
}

class CustomView(title: String, context: Context) : ViewBase<Int, Unit>(context) {

    private val text = make(::TextView, title)
    private val button = make(::Button, "Press to reset")

    override val actions: List<Observable<Unit>> = listOf(button.clicks())

    override fun loadView() {
        children(
                text,
                button
        )
    }

    private val textView = TextView("abcd", context)

    override fun setupConstraints() {
//        snp.makeConstraints {
//            left.equalTo(0)
//            top.equalTo(0)
//        }
        text.snp.makeConstraints {
            top.equalTo(50)
            left.equalTo(40)
//            height.equalTo(20)
//            width.equalTo(30)
        }
        button.snp.makeConstraints {
            centerY.equalTo(text)
            left.equalTo(text.snp.right).offset(100)
//            height.width.equalTo(50)
            height.equalTo(100)
        }
//        text.snp.debugValues()
//        button.snp.debugValues()
    }

    override fun update() {
        setBackgroundColor(Color.parseColor("#" + Integer.toHexString(componentState)))
    }
}
