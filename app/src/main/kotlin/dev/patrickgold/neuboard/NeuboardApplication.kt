/*
 * Copyright (C) 2021-2025 The Neuboard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.patrickgold.neuboard

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import androidx.core.os.UserManagerCompat
import dev.patrickgold.neuboard.app.neuboardPreferenceModel
import dev.patrickgold.neuboard.ime.clipboard.ClipboardManager
import dev.patrickgold.neuboard.ime.core.SubtypeManager
import dev.patrickgold.neuboard.ime.dictionary.DictionaryManager
import dev.patrickgold.neuboard.ime.editor.EditorInstance
import dev.patrickgold.neuboard.ime.keyboard.KeyboardManager
import dev.patrickgold.neuboard.ime.media.emoji.NeuboardEmojiCompat
import dev.patrickgold.neuboard.ime.nlp.NlpManager
import dev.patrickgold.neuboard.ime.text.gestures.GlideTypingManager
import dev.patrickgold.neuboard.ime.theme.ThemeManager
import dev.patrickgold.neuboard.lib.cache.CacheManager
import dev.patrickgold.neuboard.lib.crashutility.CrashUtility
import dev.patrickgold.neuboard.lib.devtools.Flog
import dev.patrickgold.neuboard.lib.devtools.LogTopic
import dev.patrickgold.neuboard.lib.devtools.flogError
import dev.patrickgold.neuboard.lib.devtools.flogInfo
import dev.patrickgold.neuboard.lib.devtools.flogWarning
import dev.patrickgold.neuboard.lib.ext.ExtensionManager
import dev.patrickgold.jetpref.datastore.JetPref
import org.neuboard.lib.kotlin.io.deleteContentsRecursively
import org.neuboard.lib.kotlin.tryOrNull
import org.neuboard.libnative.dummyAdd
import java.lang.ref.WeakReference

/**
 * Global weak reference for the [NeuboardApplication] class. This is needed as in certain scenarios an application
 * reference is needed, but the Android framework hasn't finished setting up
 */
private var NeuboardApplicationReference = WeakReference<NeuboardApplication?>(null)

@Suppress("unused")
class NeuboardApplication : Application() {
    companion object {
        init {
            try {
                System.loadLibrary("fl_native")
            } catch (_: Exception) {
            }
        }
    }

    private val prefs by neuboardPreferenceModel()
    private val mainHandler by lazy { Handler(mainLooper) }

    val cacheManager = lazy { CacheManager(this) }
    val clipboardManager = lazy { ClipboardManager(this) }
    val editorInstance = lazy { EditorInstance(this) }
    val extensionManager = lazy { ExtensionManager(this) }
    val glideTypingManager = lazy { GlideTypingManager(this) }
    val keyboardManager = lazy { KeyboardManager(this) }
    val nlpManager = lazy { NlpManager(this) }
    val subtypeManager = lazy { SubtypeManager(this) }
    val themeManager = lazy { ThemeManager(this) }

    override fun onCreate() {
        super.onCreate()
        NeuboardApplicationReference = WeakReference(this)
        try {
            JetPref.configure(saveIntervalMs = 500)
            Flog.install(
                context = this,
                isFloggingEnabled = BuildConfig.DEBUG,
                flogTopics = LogTopic.ALL,
                flogLevels = Flog.LEVEL_ALL,
                flogOutputs = Flog.OUTPUT_CONSOLE,
            )
            CrashUtility.install(this)
            NeuboardEmojiCompat.init(this)
            flogError { "dummy result: ${dummyAdd(3,4)}" }

            if (!UserManagerCompat.isUserUnlocked(this)) {
                cacheDir?.deleteContentsRecursively()
                extensionManager.value.init()
                registerReceiver(BootComplete(), IntentFilter(Intent.ACTION_USER_UNLOCKED))
                return
            }

            init()
        } catch (e: Exception) {
            CrashUtility.stageException(e)
            return
        }
    }

    fun init() {
        cacheDir?.deleteContentsRecursively()
        prefs.initializeBlocking(this)
        extensionManager.value.init()
        clipboardManager.value.initializeForContext(this)
        DictionaryManager.init(this)
    }

    private inner class BootComplete : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action == Intent.ACTION_USER_UNLOCKED) {
                try {
                    unregisterReceiver(this)
                } catch (e: Exception) {
                    flogError { e.toString() }
                }
                mainHandler.post { init() }
            }
        }
    }
}

private tailrec fun Context.neuboardApplication(): NeuboardApplication {
    return when (this) {
        is NeuboardApplication -> this
        is ContextWrapper -> when {
            this.baseContext != null -> this.baseContext.neuboardApplication()
            else -> NeuboardApplicationReference.get()!!
        }
        else -> tryOrNull { this.applicationContext as NeuboardApplication } ?: NeuboardApplicationReference.get()!!
    }
}

fun Context.appContext() = lazyOf(this.neuboardApplication())

fun Context.cacheManager() = this.neuboardApplication().cacheManager

fun Context.clipboardManager() = this.neuboardApplication().clipboardManager

fun Context.editorInstance() = this.neuboardApplication().editorInstance

fun Context.extensionManager() = this.neuboardApplication().extensionManager

fun Context.glideTypingManager() = this.neuboardApplication().glideTypingManager

fun Context.keyboardManager() = this.neuboardApplication().keyboardManager

fun Context.nlpManager() = this.neuboardApplication().nlpManager

fun Context.subtypeManager() = this.neuboardApplication().subtypeManager

fun Context.themeManager() = this.neuboardApplication().themeManager
