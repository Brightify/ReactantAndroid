package org.brightify.reactant.prototyping

import android.graphics.Color
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
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
import org.brightify.reactant.prototyping.CustomView.Styles.text

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
class MainWireframe : Wireframe() {

    private val navigationController = NavigationController()

    init {
        ReactantActivity.instance.addChildContainer(navigationController)
    }

    override fun entryPoint(): ViewController {
        val reactions = InitialController.Reactions {
            navigationController.push(main())
        }

        navigationController.push(InitialController(reactions))
        navigationController.toolbar.setBackgroundColor(Color.BLUE)

        return navigationController
    }

    private fun main(): MainController {
        val reactions = MainController.Reactions {
            navigationController.pop()
        }

        return MainController(reactions)
    }
}

class MainActivity : ReactantActivity(::MainWireframe)

class InitialController(private val reactions: InitialController.Reactions) : ControllerBase<Unit, CustomView, Unit>(CustomView("Initial")) {

    data class Reactions(val onNext: () -> Unit)

    override fun viewWillAppear() {
        super.viewWillAppear()

        navigationController?.setNavigationBarHidden(true)
    }

    override fun act(action: Unit) {
        reactions.onNext()
    }
}

class MainController(private val reactions: MainController.Reactions) : ControllerBase<Unit, AnotherView, Unit>(
        AnotherView()) {

    data class Reactions(val onNext: () -> Unit)

    override fun act(action: Unit) {
        reactions.onNext()
    }
}

class CustomView(title: String) : ViewBase<Int, Unit>() {

    private val text = TextView(title).apply(Styles.text)
    private val button = Button("Button")
    private val container = AutoLayout()
    private val view = TextView().apply { setBackgroundColor(Color.rgb(0, 255, 0)) }
    private val view2 = TextView().apply { setBackgroundColor(Color.rgb(0, 0, 255)) }

    override fun loadView() {
        children(
                text,
                button,
                container.children(
                        view,
                        view2
                )
        )
    }

    override fun setupConstraints() {
        text.snp.makeConstraints {
            top.equalTo(50)
            left.equalTo(40)
        }

        button.snp.makeConstraints {
            centerX.equalTo(text)
            top.equalTo(text.snp.bottom).offset(30)
        }
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

//        snp.debugValuesRecursive()
//        snp.debugConstraintsRecursive()
    }

    override val actions: List<Observable<Unit>> = listOf(button.clicks())

    override fun update() {
        setBackgroundColor(Color.parseColor("#" + Integer.toHexString(componentState)))
    }

    private object Styles {

        val text = Style<View> {
            setBackgroundColor(Color.rgb(255, 0, 0))
        }
    }
}

class AnotherView : ViewBase<Unit, Unit>() {

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