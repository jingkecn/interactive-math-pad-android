/*
 * Copyright (c) MyScript. All rights reserved.
 */

package com.myscript.iink.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.support.annotation.NonNull
import android.widget.Toast
import com.myscript.iink.Editor
import com.myscript.iink.MimeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Editor.convert() =
    getSupportedTargetConversionStates(null).firstOrNull()?.let { convert(null, it) }

fun Editor.copyToClipboard(@NonNull context: Context, type: MimeType) =
    export_(null, type).let {
        GlobalScope.launch(Dispatchers.Main) {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.run {
                primaryClip = ClipData.newPlainText(type.name, it)
                Toast.makeText(
                    context,
                    "String ($type) copied to clipboard:\n$it",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
