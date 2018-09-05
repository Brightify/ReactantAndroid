package org.brightify.reactant.controller

import android.content.res.Resources
import org.brightify.reactant.core.ReactantActivity

/**
 *  @author <a href="mailto:filip@brightify.org">Filip Dolnik</a>
 */
val ViewController.resources: Resources
        get() = ReactantActivity.context.resources