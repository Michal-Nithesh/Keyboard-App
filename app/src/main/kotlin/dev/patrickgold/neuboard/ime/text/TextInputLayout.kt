/*
 * Copyright (C) 2021-2025 The NeuBoard Contributors
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

package dev.patrickgold.neuboard.ime.text

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.aiEnhancementManager
import dev.patrickgold.neuboard.app.neuboardPreferenceModel
import dev.patrickgold.neuboard.editorInstance
import dev.patrickgold.neuboard.ime.smartbar.IncognitoDisplayMode
import dev.patrickgold.neuboard.ime.smartbar.InlineSuggestionsStyleCache
import dev.patrickgold.neuboard.ime.smartbar.Smartbar
import dev.patrickgold.neuboard.ime.smartbar.quickaction.QuickActionsOverflowPanel
import dev.patrickgold.neuboard.ime.text.keyboard.TextKeyboardLayout
import dev.patrickgold.neuboard.ime.theme.NeuboardImeUi
import dev.patrickgold.neuboard.keyboardManager
import dev.patrickgold.jetpref.datastore.model.observeAsState
import org.neuboard.lib.snygg.ui.SnyggIcon

@Composable
fun TextInputLayout(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val editorInstance by context.editorInstance()
    val aiEnhancementManager by context.aiEnhancementManager()

    val prefs by neuboardPreferenceModel()

    val state by keyboardManager.activeState.collectAsState()
    val evaluator by keyboardManager.activeEvaluator.collectAsState()

    // Update current message for AI enhancement
    val activeContent = editorInstance.activeContent
    LaunchedEffect(activeContent.text) {
        aiEnhancementManager.updateCurrentMessage(activeContent.text.toString())
    }

    InlineSuggestionsStyleCache()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Smartbar()
            if (state.isActionsOverflowVisible) {
                QuickActionsOverflowPanel()
            } else {
                Box {
                    val incognitoDisplayMode by prefs.keyboard.incognitoDisplayMode.observeAsState()
                    val showIncognitoIcon = evaluator.state.isIncognitoMode &&
                        incognitoDisplayMode == IncognitoDisplayMode.DISPLAY_BEHIND_KEYBOARD
                    if (showIncognitoIcon) {
                        SnyggIcon(
                            NeuboardImeUi.IncognitoModeIndicator.elementName,
                            modifier = Modifier
                                .matchParentSize()
                                .align(Alignment.Center),
                            painter = painterResource(R.drawable.ic_incognito),
                        )
                    }
                    TextKeyboardLayout(evaluator = evaluator)
                }
            }
        }
    }
}
