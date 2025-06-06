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

package dev.patrickgold.neuboard.ime.smartbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.aiEnhancementManager
import dev.patrickgold.neuboard.app.neuboardPreferenceModel
import dev.patrickgold.neuboard.editorInstance
import dev.patrickgold.neuboard.ime.editor.AbstractEditorInstance
import dev.patrickgold.neuboard.ime.ai.EnhanceButton
import dev.patrickgold.neuboard.ime.ai.MessageEnhancementDialog
import dev.patrickgold.neuboard.ime.ai.QuickReplySuggestionsRow
import dev.patrickgold.neuboard.ime.keyboard.NeuboardImeSizing
import dev.patrickgold.neuboard.ime.nlp.NlpInlineAutofill
import dev.patrickgold.neuboard.ime.smartbar.quickaction.QuickActionButton
import dev.patrickgold.neuboard.ime.smartbar.quickaction.QuickActionsRow
import dev.patrickgold.neuboard.ime.smartbar.quickaction.ToggleOverflowPanelAction
import dev.patrickgold.neuboard.ime.theme.NeuboardImeUi
import dev.patrickgold.neuboard.keyboardManager
import dev.patrickgold.neuboard.lib.compose.horizontalTween
import dev.patrickgold.neuboard.lib.compose.verticalTween
import dev.patrickgold.neuboard.nlpManager
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.datastore.ui.vectorResource
import org.neuboard.lib.android.AndroidVersion
import org.neuboard.lib.snygg.ui.SnyggBox
import org.neuboard.lib.snygg.ui.SnyggColumn
import org.neuboard.lib.snygg.ui.SnyggIcon
import org.neuboard.lib.snygg.ui.SnyggIconButton
import org.neuboard.lib.snygg.ui.SnyggRow
import org.neuboard.lib.snygg.ui.rememberSnyggThemeQuery

private const val AnimationDuration = 200

private val VerticalEnterTransition = EnterTransition.verticalTween(AnimationDuration)
private val VerticalExitTransition = ExitTransition.verticalTween(AnimationDuration)

private val HorizontalEnterTransition = EnterTransition.horizontalTween(AnimationDuration)
private val HorizontalExitTransition = ExitTransition.horizontalTween(AnimationDuration)

private val NoEnterTransition = EnterTransition.horizontalTween(0)
private val NoExitTransition = ExitTransition.horizontalTween(0)

private val AnimationTween = tween<Float>(AnimationDuration)
private val NoAnimationTween = tween<Float>(0)

@Composable
fun Smartbar() {
    val prefs by neuboardPreferenceModel()
    val smartbarEnabled by prefs.smartbar.enabled.observeAsState()
    val extendedActionsPlacement by prefs.smartbar.extendedActionsPlacement.observeAsState()

    AnimatedVisibility(
        visible = smartbarEnabled,
        enter = VerticalEnterTransition,
        exit = VerticalExitTransition,
    ) {
        when (extendedActionsPlacement) {
            ExtendedActionsPlacement.ABOVE_CANDIDATES -> {
                SnyggColumn(NeuboardImeUi.Smartbar.elementName) {
                    SmartbarSecondaryRow()
                    SmartbarMainRow()
                }
            }

            ExtendedActionsPlacement.BELOW_CANDIDATES -> {
                SnyggColumn(NeuboardImeUi.Smartbar.elementName) {
                    SmartbarMainRow()
                    SmartbarSecondaryRow()
                }
            }

            ExtendedActionsPlacement.OVERLAY_APP_UI -> {
                SnyggBox(NeuboardImeUi.Smartbar.elementName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NeuboardImeSizing.smartbarHeight),
                    allowClip = false,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(NeuboardImeSizing.smartbarHeight * 2)
                            .absoluteOffset(y = -NeuboardImeSizing.smartbarHeight),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        SmartbarSecondaryRow()
                    }
                    SmartbarMainRow()
                }
            }
        }
    }
}

@Composable
private fun SmartbarMainRow(modifier: Modifier = Modifier) {
    val prefs by neuboardPreferenceModel()
    val context = LocalContext.current
    val keyboardManager by context.keyboardManager()
    val nlpManager by context.nlpManager()

    val inlineSuggestions by NlpInlineAutofill.suggestions.collectAsState()
    LaunchedEffect(inlineSuggestions) {
        nlpManager.autoExpandCollapseSmartbarActions(null, inlineSuggestions)
    }
    val shouldShowInlineSuggestionsUi = AndroidVersion.ATLEAST_API30_R && inlineSuggestions.isNotEmpty()

    val smartbarLayout by prefs.smartbar.layout.observeAsState()
    val flipToggles by prefs.smartbar.flipToggles.observeAsState()
    val sharedActionsExpanded by prefs.smartbar.sharedActionsExpanded.observeAsState()
    val extendedActionsExpanded by prefs.smartbar.extendedActionsExpanded.observeAsState()

    val shouldAnimate by prefs.smartbar.sharedActionsExpandWithAnimation.observeAsState()

    @Composable
    fun SharedActionsToggle() {
        SnyggIconButton(
            elementName = NeuboardImeUi.SmartbarSharedActionsToggle.elementName,
            onClick = {
                if (/* was */ sharedActionsExpanded) {
                    keyboardManager.activeState.isActionsOverflowVisible = false
                }
                prefs.smartbar.sharedActionsExpanded.set(!sharedActionsExpanded)
            },
            modifier = Modifier.sizeIn(maxHeight = NeuboardImeSizing.smartbarHeight).aspectRatio(1f)
        ) {
            val transition = updateTransition(sharedActionsExpanded, label = "sharedActionsExpandedToggleBtn")
            val rotation by transition.animateFloat(
                transitionSpec = {
                    if (shouldAnimate) AnimationTween else NoAnimationTween
                },
                label = "rotation",
            ) {
                if (it) 180f else 0f
            }
            val arrowIcon = if (flipToggles) {
                Icons.AutoMirrored.Default.KeyboardArrowLeft
            } else {
                Icons.AutoMirrored.Default.KeyboardArrowRight
            }
            val incognitoIcon = vectorResource(id = R.drawable.ic_incognito)
            val incognitoDisplayMode = prefs.keyboard.incognitoDisplayMode.observeAsState()
            val isIncognitoMode = keyboardManager.activeState.isIncognitoMode
            val icon = if (isIncognitoMode) {
                when (incognitoDisplayMode.value) {
                    IncognitoDisplayMode.REPLACE_SHARED_ACTIONS_TOGGLE -> incognitoIcon!!
                    IncognitoDisplayMode.DISPLAY_BEHIND_KEYBOARD -> arrowIcon
                }
            } else {
                arrowIcon
            }
            SnyggIcon(
                modifier = Modifier.rotate(if (incognitoDisplayMode.value == IncognitoDisplayMode.DISPLAY_BEHIND_KEYBOARD) rotation else 0f),
                imageVector = icon,
            )
        }
    }

    @Composable
    fun RowScope.CenterContent() {
        val expanded = sharedActionsExpanded && smartbarLayout == SmartbarLayout.SUGGESTIONS_ACTIONS_SHARED
        val aiEnhancementManager by context.aiEnhancementManager()
        val quickReplySuggestions by aiEnhancementManager.quickReplySuggestions.collectAsState()
        val hasQuickReplies = quickReplySuggestions.isNotEmpty()
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(NeuboardImeSizing.smartbarHeight),
        ) {
            val enterTransition = if (shouldAnimate) HorizontalEnterTransition else NoEnterTransition
            val exitTransition = if (shouldAnimate) HorizontalExitTransition else NoExitTransition
            androidx.compose.animation.AnimatedVisibility(
                visible = !expanded,
                enter = enterTransition,
                exit = exitTransition,
            ) {
                if (shouldShowInlineSuggestionsUi) {
                    InlineSuggestionsUi(inlineSuggestions)
                } else if (hasQuickReplies) {
                    QuickReplySuggestionsRow()
                } else {
                    CandidatesRow()
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = expanded,
                enter = enterTransition,
                exit = exitTransition,
            ) {
                QuickActionsRow(
                    NeuboardImeUi.SmartbarSharedActionsRow.elementName,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(NeuboardImeSizing.smartbarHeight),
                )
            }
        }
    }

    @Composable
    fun ExtendedActionsToggle() {
        SnyggIconButton(
            NeuboardImeUi.SmartbarExtendedActionsToggle.elementName,
            onClick = {
                if (/* was */ extendedActionsExpanded) {
                    keyboardManager.activeState.isActionsOverflowVisible = false
                }
                prefs.smartbar.extendedActionsExpanded.set(!extendedActionsExpanded)
            },
            modifier = Modifier.sizeIn(maxHeight = NeuboardImeSizing.smartbarHeight).aspectRatio(1f)
        ) {
            val transition = updateTransition(extendedActionsExpanded, label = "smartbarSecondaryRowToggleBtn")
            val alpha by transition.animateFloat(label = "alpha") { if (it) 1f else 0f }
            val rotation by transition.animateFloat(label = "rotation") { if (it) 180f else 0f }
            // Expanded icon
            SnyggIcon(
                NeuboardImeUi.SmartbarExtendedActionsToggle.elementName,
                modifier = Modifier
                    .alpha(alpha)
                    .rotate(rotation),
                imageVector = Icons.Default.UnfoldLess,
            )
            // Not expanded icon
            SnyggIcon(
                NeuboardImeUi.SmartbarExtendedActionsToggle.elementName,
                modifier = Modifier
                    .alpha(1f - alpha)
                    .rotate(rotation - 180f),
                imageVector = Icons.Default.UnfoldMore,
            )
        }
    }

    @Composable
    fun StickyAction() {
        val actionArrangement by prefs.smartbar.actionArrangement.observeAsState()
        val evaluator by keyboardManager.activeSmartbarEvaluator.collectAsState()
        val aiEnhancementManager by context.aiEnhancementManager()
        val currentMessage by aiEnhancementManager.currentMessage.collectAsState()
        val editorInstance = context.editorInstance().value
        val isEditorEnabled = editorInstance.activeInfo.isRichInputEditor && currentMessage.isNotEmpty()

        val action = when {
            actionArrangement.stickyAction != null -> {
                actionArrangement.stickyAction
            }

            smartbarLayout == SmartbarLayout.SUGGESTIONS_ACTIONS_SHARED && sharedActionsExpanded -> {
                ToggleOverflowPanelAction
            }

            else -> null
        }

        if (action != null) {
            if (isEditorEnabled) {
                EnhanceButton(
                    onClick = {
                        aiEnhancementManager.showEnhancementDialog()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                QuickActionButton(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    action = action,
                    evaluator = evaluator,
                )
            }
        } else {
            if (isEditorEnabled) {
                EnhanceButton(
                    onClick = {
                        aiEnhancementManager.showEnhancementDialog()
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .aspectRatio(1f),
                )
            }
        }
        
        if (aiEnhancementManager.isEnhancementDialogVisible) {
            MessageEnhancementDialog(
                originalMessage = currentMessage,
                onDismiss = { aiEnhancementManager.hideEnhancementDialog() },
                onMessageEnhanced = { enhancedMessage: String -> 
                    val editor = context.editorInstance().value
                    editor.deleteBackwards()
                    editor.commitText(enhancedMessage)
                },
                enhancerService = aiEnhancementManager.enhancerService
            )
        }
    }

    SideEffect {
        if (!shouldAnimate) {
            prefs.smartbar.sharedActionsExpandWithAnimation.set(true)
        }
    }

    SnyggRow(
        modifier = modifier
            .fillMaxWidth()
            .height(NeuboardImeSizing.smartbarHeight),
    ) {
        when (smartbarLayout) {
            SmartbarLayout.SUGGESTIONS_ONLY -> {
                if (shouldShowInlineSuggestionsUi) {
                    InlineSuggestionsUi(inlineSuggestions)
                } else {
                    CandidatesRow()
                }
            }

            SmartbarLayout.ACTIONS_ONLY -> {
                if (shouldShowInlineSuggestionsUi) {
                    InlineSuggestionsUi(inlineSuggestions)
                } else {
                    QuickActionsRow(NeuboardImeUi.SmartbarSharedActionsRow.elementName)
                }
            }

            SmartbarLayout.SUGGESTIONS_ACTIONS_SHARED -> {
                if (!flipToggles) {
                    SharedActionsToggle()
                    CenterContent()
                    StickyAction()
                } else {
                    StickyAction()
                    CenterContent()
                    SharedActionsToggle()
                }
            }

            SmartbarLayout.SUGGESTIONS_ACTIONS_EXTENDED -> {
                if (!flipToggles) {
                    ExtendedActionsToggle()
                    CenterContent()
                    StickyAction()
                } else {
                    StickyAction()
                    CenterContent()
                    ExtendedActionsToggle()
                }
            }
        }
    }
}

@Composable
private fun SmartbarSecondaryRow(modifier: Modifier = Modifier) {
    val prefs by neuboardPreferenceModel()
    val smartbarLayout by prefs.smartbar.layout.observeAsState()
    val secondaryRowStyle = rememberSnyggThemeQuery(NeuboardImeUi.SmartbarExtendedActionsRow.elementName)
    val windowStyle = rememberSnyggThemeQuery(NeuboardImeUi.Window.elementName)
    val extendedActionsExpanded by prefs.smartbar.extendedActionsExpanded.observeAsState()
    val extendedActionsPlacement by prefs.smartbar.extendedActionsPlacement.observeAsState()
    val background = secondaryRowStyle.background().let { color ->
        if (extendedActionsPlacement == ExtendedActionsPlacement.OVERLAY_APP_UI) {
            if (color.isUnspecified || color.alpha == 0f) {
                windowStyle.background(default = Color.Black)
            } else {
                color
            }
        } else {
            color
        }
    }

    AnimatedVisibility(
        visible = smartbarLayout == SmartbarLayout.SUGGESTIONS_ACTIONS_EXTENDED && extendedActionsExpanded,
        enter = VerticalEnterTransition,
        exit = VerticalExitTransition,
    ) {
        QuickActionsRow(
            NeuboardImeUi.SmartbarExtendedActionsRow.elementName,
            modifier = modifier
                .fillMaxWidth()
                .height(NeuboardImeSizing.smartbarHeight)
                .background(background),
        )
    }
}
