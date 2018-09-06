package org.brightify.reactant.prototyping

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import org.brightify.reactant.autolayout.Constraint
import org.brightify.reactant.autolayout.ConstraintPriority
import org.brightify.reactant.autolayout.util.children
import org.brightify.reactant.autolayout.util.snp
import org.brightify.reactant.controller.NavigationController
import org.brightify.reactant.controller.ViewController
import org.brightify.reactant.core.ControllerBase
import org.brightify.reactant.core.ReactantActivity
import org.brightify.reactant.core.ViewBase
import org.brightify.reactant.core.Wireframe
import org.brightify.reactant.core.util.Button
import org.brightify.reactant.core.util.Style
import org.brightify.reactant.core.util.TextView

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
class MainWireframe: Wireframe() {

    private val navigationController = NavigationController()

    override fun entrypoint(): ViewController {
        //        val reactions = InitialController.Reactions {
        //            navigationController.push(main())
        //        }

        navigationController.push(InitialController())
        //        navigationController.push(InitialController(reactions))
        return navigationController
    }

    //    private fun main(): MainController {
    //        val reactions = MainController.Reactions {
    //            navigationController.pop()
    //        }
    //
    //        return MainController(reactions)
    //    }
}

class MainActivity: ReactantActivity(::MainWireframe)

val initialValue = 50f

//class InitialController(private val reactions: InitialController.Reactions): ControllerBase<Unit, InitialController.CustomView, Unit>(CustomView("Initial")) {
class InitialController: ControllerBase<Unit, CustomRootView, AnimationAction>(Unit, { CustomRootView(it, initialValue) }) {

    private val animator = ValueAnimator.ofFloat(initialValue.toFloat(), 150.toFloat(), initialValue.toFloat())

    data class Reactions(val onNext: () -> Unit)

    override fun viewWillAppear() {
        super.viewWillAppear()

        navigationController?.toolbar?.setBackgroundColor(Color.BLUE)
        navigationController?.setNavigationBarHidden(true)

        animator.addUpdateListener {
            rootView.componentState = it.animatedValue as Float
        }
    }

    override fun act(action: AnimationAction) {
        when (action) {
            AnimationAction.ANIMATE -> {
                if (!animator.isStarted) {
                    animator.setFloatValues(initialValue.toFloat(), 150.toFloat(), initialValue.toFloat())
                    animator.duration = 2000
                    animator.start()
//                } else if (animator.isPaused) {
//                    animator.resume()
//                } else {
//                    animator.pause()
                }
            }
            AnimationAction.RESET -> {
                if (animator.isStarted) {
                    animator.reverse()
                }
            }
        }
    }
}

enum class AnimationAction {
    ANIMATE, RESET
}

class CustomRootView(context: Context, initialValue: Float): ViewBase<Float, AnimationAction>(context, initialValue) {

    override val actions: List<Observable<AnimationAction>>
        get() = listOf(
                animateButton.clicks().map { AnimationAction.ANIMATE },
                resetButton.clicks().map { AnimationAction.RESET }
        )

    private val constraints = ArrayList<Constraint>()

    private val animateButton = Button("Animate").apply(Styles.text)
    private val resetButton = Button("Reset").apply(Styles.text)
    private val views = (0 until 4).map { TextView().apply { setBackgroundColor(Color.rgb(0, it * 100, 0)) } }
    private val text = TextView("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")

    override fun loadView() {
        children(
                animateButton,
                resetButton,
                text
        )
        views.forEach { children(it) }

        //        measureTime = true
    }

    override fun setupConstraints() {
        animateButton.snp.makeConstraints {
            top.equalTo(20)
            left.equalTo(20)
        }

        resetButton.snp.makeConstraints {
            top.equalTo(20)
            left.equalTo(animateButton).offset(200)
        }

        views.subList(0, 2).forEachIndexed { index, view ->
            view.snp.makeConstraints {
                left.equalToSuperview().offset(250).priority(ConstraintPriority.high)
                top.equalToSuperview().offset(100 + 80 * index).priority(ConstraintPriority.high)
                size.equalTo(20).priority(ConstraintPriority.high)
            }
        }

        views.subList(2, 4).forEachIndexed { index, view ->
            view.snp.makeConstraints {
                left.equalToSuperview().offset(50).priority(ConstraintPriority.high)
                top.equalToSuperview().offset(100 + 80 * index).priority(ConstraintPriority.high)
                size.equalTo(20).priority(ConstraintPriority.high)
            }
        }

        views[0].snp.makeConstraints {
            constraints.add(size.equalTo(initialValue))
        }
        views[1].snp.makeConstraints {
            constraints.add(width.equalTo(initialValue))
        }
        views[2].snp.makeConstraints {
            constraints.add(height.equalTo(initialValue))
        }
        views[3].snp.makeConstraints {
            constraints.add(left.equalTo(initialValue))
        }

        text.snp.makeConstraints {
            left.equalTo(50)
            top.equalTo(300)
            constraints.add(width.equalTo(20))
        }
    }

    override fun update() {
        constraints.forEach { it.offset(componentState) }
    }

    private object Styles {

        val text = Style<View> {
            setBackgroundColor(Color.rgb(255, 0, 0))
        }
    }
}

//
//class MainController(private val reactions: MainController.Reactions): ControllerBase<Unit, AnotherView, Unit>(
//        AnotherView()) {
//
//    data class Reactions(val onNext: () -> Unit)
//
//    override fun act(action: Unit) {
//        reactions.onNext()
//    }
//}
//
//class CustomView(title: String): ViewBase<Int, Unit>() {
//
//
//    override val actions: List<Observable<Unit>>
//        get() = listOf(button1.clicks())
//
//    private val button1 = Button(title).apply(Styles.text)
//    val buttonContainer1 = AutoLayout()
//    val buttonContainer2 = AutoLayout()
//    private val button2 = Button("Button")
//    private val container = AutoLayout()
//    private val view = TextView().apply { setBackgroundColor(Color.rgb(0, 255, 0)) }
//    private val view2 = TextView().apply { setBackgroundColor(Color.rgb(0, 0, 255)) }
//
//    override fun loadView() {
//        children(
//                button1,
//                //                buttonContainer1.children(buttonContainer2.children(button2))
//                //                ,
//                container.children(
//                        view,
//                        view2
//                )
//        )
//        buttonContainer1.children(buttonContainer2.children(button2))
//
//        button1.clicks().subscribe {
//            if (children.contains(buttonContainer1)) {
//                removeView(buttonContainer1)
//            } else {
//                addView(buttonContainer1)
//                buttonContainer1.snp.remakeConstraints {
//                    centerX.equalTo(button1)
//                    top.equalTo(button1.snp.bottom).offset(30)
//                }
//            }
//        }
//        measureTime = true
//    }
//
//    override fun setupConstraints() {
//        button1.snp.makeConstraints {
//            top.equalTo(50)
//            left.equalTo(40)
//        }
//        //        buttonContainer1.snp.remakeConstraints {
//        //            centerX.equalTo(button1)
//        //            top.equalTo(button1.snp.bottom).offset(30)
//        //        }
//        buttonContainer2.snp.makeConstraints {
//            edges.equalToSuperview()
//        }
//
//        button2.snp.makeConstraints {
//            edges.equalToSuperview()
//            height.equalTo(36)
//        }
//        button2.setPadding(32, 0, 32, 0)
//        container.snp.makeConstraints {
//            bottom.right.equalToSuperview()
//            width.height.equalTo(150)
//        }
//        view.snp.makeConstraints {
//            right.left.equalToSuperview()
//            top.equalToSuperview()
//        }
//        view2.snp.makeConstraints {
//            top.equalTo(view.snp.bottom)
//            bottom.left.right.equalToSuperview()
//            height.equalTo(50)
//        }
//    }
//
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//
//        //        snp.debugValuesRecursive()
//        //        snp.debugConstraintsRecursive()
//    }
//
//    //    override val actions: List<Observable<Unit>> = listOf(button1.clicks())
//
//    override fun update() {
//        setBackgroundColor(Color.parseColor("#" + Integer.toHexString(componentState)))
//    }
//
//    private object Styles {
//
//        val text = Style<View> {
//            setBackgroundColor(Color.rgb(255, 0, 0))
//        }
//    }
//}
//
//class AnotherView: ViewBase<Unit, Unit>() {
//
//    private val button = Button("Main")
//
//    override val actions: List<Observable<Unit>> = listOf(button.clicks())
//
//    override fun loadView() {
//        children(button)
//
//        button.textSize = 30f
//        button.setBackgroundColor(Color.LTGRAY)
//    }
//
//    override fun setupConstraints() {
//        button.snp.makeConstraints {
//            center.equalToSuperview()
//        }
//    }
//
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//
//        //        snp.debugValuesRecursive()
//        //        snp.debugConstraintsRecursive()
//    }
//}