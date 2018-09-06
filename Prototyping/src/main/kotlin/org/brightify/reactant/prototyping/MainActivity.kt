package org.brightify.reactant.prototyping

import android.content.Context
import android.graphics.Color
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.controller.NavigationController
import org.brightify.reactant.controller.ViewController
import org.brightify.reactant.core.ControllerBase
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.ViewBase
import org.brightify.reactant.core.Wireframe
import org.brightify.reactant.core.util.AutoLayout
import org.brightify.reactant.core.util.Button
import org.brightify.reactant.core.util.Style
import org.brightify.reactant.core.util.TextView

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class MainActivity: ReactantActivity(::MainWireframe)

class MainWireframe: Wireframe() {

    private val navigationController = NavigationController()

    override fun entrypoint(): ViewController {
        val reactions = InitialController.Reactions {
            navigationController.push(main())
        }

        navigationController.push(InitialController(reactions))
        return navigationController
    }

    private fun main(): MainController {
        val reactions = MainController.Reactions {
            navigationController.pop()
        }

        return MainController(reactions)
    }
}

class InitialController(private val reactions: InitialController.Reactions): ControllerBase<Unit, CustomView, Unit>(Unit, { CustomView(it, "Initial") }) {

    data class Reactions(val onNext: () -> Unit)

    override fun act(action: Unit) {
        rootView.componentState = when (rootView.componentState) {
            "red" -> "green"
            "green" -> "blue"
            else -> "red"
        }
        reactions.onNext()
    }
}


class MainController(private val reactions: MainController.Reactions): ControllerBase<Unit, AnotherView, Unit>(Unit, ::AnotherView) {

    data class Reactions(val onNext: () -> Unit)

    override fun act(action: Unit) {
        reactions.onNext()
    }
}

class CustomView(context: Context, title: String): ViewBase<String, Unit>(context, "blue") {


    override val actions: List<Observable<Unit>>
        get() = listOf(button1.clicks())

    private val button1 = Button(title).apply(Styles.text)
    val buttonContainer1 = AutoLayout()
    val buttonContainer2 = AutoLayout()
    private val button2 = Button("Button")
    private val container = AutoLayout()
    private val view = TextView().apply { setBackgroundColor(Color.rgb(0, 255, 0)) }
    private val view2 = TextView().apply { setBackgroundColor(Color.rgb(0, 0, 255)) }

    override fun loadView() {
        children(
                button1,
                //                buttonContainer1.children(buttonContainer2.children(button2))
                //                ,
                container.children(
                        view,
                        view2
                )
        )
        buttonContainer1.children(buttonContainer2.children(button2))

        button1.clicks().subscribe {
            if (children.contains(buttonContainer1)) {
                removeView(buttonContainer1)
            } else {
                addView(buttonContainer1)
                buttonContainer1.snp.remakeConstraints {
                    centerX.equalTo(button1)
                    top.equalTo(button1.snp.bottom).offset(30)
                }
            }
        }
        measureTime = true
    }

    override fun setupConstraints() {
        button1.snp.makeConstraints {
            top.equalTo(50)
            left.equalTo(40)
        }
        //        buttonContainer1.snp.remakeConstraints {
        //            centerX.equalTo(button1)
        //            top.equalTo(button1.snp.bottom).offset(30)
        //        }
        buttonContainer2.snp.makeConstraints {
            edges.equalToSuperview()
        }

        button2.snp.makeConstraints {
            edges.equalToSuperview()
            height.equalTo(36)
        }
        button2.setPadding(32, 0, 32, 0)
        container.snp.makeConstraints {
            bottom.right.equalToSuperview()
            width.height.equalTo(150)
        }
        view.snp.makeConstraints {
            right.left.equalToSuperview()
            top.equalToSuperview()
        }
        view2.snp.makeConstraints {
            top.equalTo(view.snp.bottom)
            bottom.left.right.equalToSuperview()
            height.equalTo(50)
        }
    }

//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)

        //        snp.debugValuesRecursive()
        //        snp.debugConstraintsRecursive()
//    }

    //    override val actions: List<Observable<Unit>> = listOf(button1.clicks())

    override fun update(previousComponentState: String?) {
        setBackgroundColor(Color.parseColor(componentState))
    }

    private object Styles {

        val text = Style<View> {
            setBackgroundColor(Color.rgb(255, 0, 0))
        }
    }
}

class AnotherView(context: Context): ViewBase<Unit, Unit>(context, Unit) {

    private val button = Button("Main")

    override val actions: List<Observable<Unit>> = listOf(button.clicks())

    override fun loadView() {
        children(button)

        button.textSize = 30f
        button.setBackgroundColor(Color.LTGRAY)
    }

    override fun setupConstraints() {
        button.snp.makeConstraints {
            center.equalToSuperview()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        //        snp.debugValuesRecursive()
        //        snp.debugConstraintsRecursive()
    }
}