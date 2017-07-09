package org.brightify.reactant.core

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.UUID

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal class ViewControllerWrapper() : Fragment() {

    private val InstanceUUIDKey = "ViewController"

    val activity: ReactantActivity?
        get() = getActivity() as? ReactantActivity

    var viewController: ViewController? = null

    constructor(viewController: ViewController) : this() {
        this.viewController = viewController
        viewController.viewControllerWrapper = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewController?.onCreate()

        savedInstanceState?.getString(InstanceUUIDKey)?.let {
            viewController = InstanceStorage.retrieve(it)
            viewController?.viewControllerWrapper = this
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return viewController?.contentView ?: View(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewController?.onActivityCreated()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        viewController?.let {
            if (outState != null) {
                val uuid = UUID.randomUUID().toString()
                outState.putString(InstanceUUIDKey, uuid)
                InstanceStorage.store(uuid, it)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewController?.onStart()
    }

    override fun onResume() {
        super.onResume()

        viewController?.onResume()
    }

    override fun onPause() {
        super.onPause()

        viewController?.onPause()
    }

    override fun onStop() {
        super.onStop()

        viewController?.onStop()
    }
}