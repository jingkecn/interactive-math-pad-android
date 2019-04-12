/*
 * Copyright (c) MyScript. All rights reserved.
 */

package com.myscript.iink.app.mathpad

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.myscript.iink.*
import com.myscript.iink.extensions.convert
import com.myscript.iink.extensions.copyToClipboard
import com.myscript.iink.uireferenceimplementation.EditorView
import com.myscript.iink.uireferenceimplementation.FontUtils
import com.myscript.iink.uireferenceimplementation.InputController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), IEditorListener {

    private lateinit var contentPart: ContentPart
    private lateinit var editorView: EditorView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as? MyApplication)?.contentPackage?.let { initWith(it) }
        editorView = findViewById<EditorView>(R.id.editor_view).also { initWith(it) }
    }

    private fun initWith(contentPackage: ContentPackage) = with(contentPackage) {
        createPart("Math").let { contentPart = it }
    }

    private fun initWith(view: EditorView) = with(view) {
        (application as? IInteractiveInkApplication)?.engine?.let {
            setEngine(it)
            editor?.addListener(this@MainActivity)
            inputMode = InputController.INPUT_MODE_FORCE_PEN
            setTypefaces(FontUtils.loadFontsFromAssets(applicationContext.assets))
            post {
                editor?.run {
                    val strokeColor =
                        resources.getColor(R.color.zhYanLan).and(0xFFFFFF).toString(16)
                    val theme =
                        resources.getString(R.string.editor_theme, "#$strokeColor")
                    setTheme(theme)
                    part = contentPart
                }
                visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        editorView.close()
        contentPart.close()
        super.onDestroy()
    }

    // region Implementations (options menu)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            editorView.editor?.run {
                it.findItem(R.id.menu_clear)?.isEnabled = part?.isClosed == false
                it.findItem(R.id.menu_redo)?.isEnabled = canRedo()
                it.findItem(R.id.menu_undo)?.isEnabled = canUndo()
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        editorView.editor?.let {
            // wait for the editor to be idle.
            if (!it.isIdle) it.waitForIdle()
            when (item?.itemId) {
                R.id.menu_clear -> it.clear()
                R.id.menu_convert -> it.convert()
                R.id.menu_redo -> it.redo()
                R.id.menu_undo -> it.undo()
                R.id.menu_export_latex -> it.copyToClipboard(this, MimeType.LATEX)
                R.id.menu_export_math_ml -> it.copyToClipboard(this, MimeType.MATHML)
                else -> return@let
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // endregion

    // region Implementations (IEditorListener)

    override fun contentChanged(editor: Editor?, blockIds: Array<out String>?) {
        invalidateOptionsMenu()
    }

    override fun partChanging(editor: Editor?, old: ContentPart?, new: ContentPart?) {
        invalidateOptionsMenu()
    }

    override fun partChanged(editor: Editor?) {
        invalidateOptionsMenu()
    }

    override fun onError(editor: Editor?, blockId: String?, message: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    // endregion
}
