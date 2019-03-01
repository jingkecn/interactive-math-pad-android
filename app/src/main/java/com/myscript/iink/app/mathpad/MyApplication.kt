/*
 * Copyright (c) MyScript. All rights reserved.
 */

package com.myscript.iink.app.mathpad

import android.app.Application
import com.myscript.certificate.MyCertificate
import com.myscript.iink.Engine
import java.io.File

@Suppress("unused")
class MyApplication : Application(), IInteractiveInkApplication {

    override lateinit var engine: Engine
        private set

    override fun onCreate() {
        super.onCreate()
        // Create MyScript interactive ink engine.
        // Please make sure that you have a valid active certificate.
        // If not, please get one from MyScript Developer:
        // - https://developer.myscript.com/getting-started
        engine = Engine.create(MyCertificate.getBytes()).apply {
            // configure MyScript interactive ink engine.
            configuration?.let {
                // configure the directories where to find *.conf.
                it.setStringArray(
                    "configuration-manager.search-path",
                    arrayOf("zip://$packageCodePath!/assets/conf")
                )
                // configure a temporary directory.
                it.setString("content-package.temp-folder", "${filesDir.path}${File.separator}tmp")
            }
        }
    }

    override fun onTerminate() {
        // close MyScript iink runtime to release resources.
        engine.close()
        super.onTerminate()
    }
}
