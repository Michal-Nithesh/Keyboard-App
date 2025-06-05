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

package dev.patrickgold.neuboard.ime.theme

/*
import org.neuboard.lib.snygg.Snygg
import org.neuboard.lib.snygg.SnyggLevel
import org.neuboard.lib.snygg.SnyggPropertySetSpecDeclBuilder
import org.neuboard.lib.snygg.SnyggSpecDecl
import org.neuboard.lib.snygg.value.SnyggCircleShapeValue
import org.neuboard.lib.snygg.value.SnyggCutCornerDpShapeValue
import org.neuboard.lib.snygg.value.SnyggCutCornerPercentShapeValue
import org.neuboard.lib.snygg.value.SnyggDpSizeValue
import org.neuboard.lib.snygg.value.SnyggDynamicColorDarkColorValue
import org.neuboard.lib.snygg.value.SnyggDynamicColorLightColorValue
import org.neuboard.lib.snygg.value.SnyggRectangleShapeValue
import org.neuboard.lib.snygg.value.SnyggRoundedCornerDpShapeValue
import org.neuboard.lib.snygg.value.SnyggRoundedCornerPercentShapeValue
import org.neuboard.lib.snygg.value.SnyggStaticColorValue
import org.neuboard.lib.snygg.value.SnyggSpSizeValue

fun SnyggPropertySetSpecDeclBuilder.background() {
    property(
        name = Snygg.Background,
        level = SnyggLevel.BASIC,
        supportedValues(SnyggStaticColorValue, SnyggDynamicColorLightColorValue, SnyggDynamicColorDarkColorValue),
    )
}
fun SnyggPropertySetSpecDeclBuilder.foreground() {
    property(
        name = Snygg.Foreground,
        level = SnyggLevel.BASIC,
        supportedValues(SnyggStaticColorValue, SnyggDynamicColorLightColorValue, SnyggDynamicColorDarkColorValue),
    )
}
fun SnyggPropertySetSpecDeclBuilder.border() {
    property(
        name = Snygg.BorderColor,
        level = SnyggLevel.ADVANCED,
        supportedValues(SnyggStaticColorValue, SnyggDynamicColorLightColorValue, SnyggDynamicColorDarkColorValue),
    )
    property(
        name = Snygg.BorderWidth,
        level = SnyggLevel.ADVANCED,
        supportedValues(SnyggDpSizeValue),
    )
}
fun SnyggPropertySetSpecDeclBuilder.font() {
    property(
        name = Snygg.FontSize,
        level = SnyggLevel.ADVANCED,
        supportedValues(SnyggSpSizeValue),
    )
}
fun SnyggPropertySetSpecDeclBuilder.shadow() {
    property(
        name = Snygg.ShadowElevation,
        level = SnyggLevel.ADVANCED,
        supportedValues(SnyggDpSizeValue),
    )
}
fun SnyggPropertySetSpecDeclBuilder.shape() {
    property(
        name = Snygg.Shape,
        level = SnyggLevel.ADVANCED,
        supportedValues(
            SnyggRectangleShapeValue,
            SnyggCircleShapeValue,
            SnyggRoundedCornerDpShapeValue,
            SnyggRoundedCornerPercentShapeValue,
            SnyggCutCornerDpShapeValue,
            SnyggCutCornerPercentShapeValue,
        ),
    )
}

object NeuImeUiSpec : SnyggSpecDecl({
    element(NeuImeUi.Keyboard) {
        background()
    }
    element(NeuImeUi.Key) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.KeyHint) {
        background()
        foreground()
        font()
        shape()
    }
    element(NeuImeUi.KeyPopup) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }

    element(NeuImeUi.Smartbar) {
        background()
    }
    element(NeuImeUi.SmartbarSharedActionsRow) {
        background()
    }
    element(NeuImeUi.SmartbarSharedActionsToggle) {
        background()
        foreground()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.SmartbarExtendedActionsRow) {
        background()
    }
    element(NeuImeUi.SmartbarExtendedActionsToggle) {
        background()
        foreground()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.SmartbarActionKey) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.SmartbarActionTile) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.SmartbarActionsOverflowCustomizeButton) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.SmartbarActionsOverflow) {
        background()
    }
    element(NeuImeUi.SmartbarActionsEditor) {
        background()
        shape()
    }
    element(NeuImeUi.SmartbarActionsEditorHeader) {
        background()
        foreground()
        font()
    }
    element(NeuImeUi.SmartbarActionsEditorSubheader) {
        foreground()
        font()
    }
    element(NeuImeUi.SmartbarCandidatesRow) {
        background()
    }
    element(NeuImeUi.SmartbarCandidateWord) {
        background()
        foreground()
        font()
        shape()
    }
    element(NeuImeUi.SmartbarCandidateClip) {
        background()
        foreground()
        font()
        shape()
    }
    element(NeuImeUi.SmartbarCandidateSpacer) {
        foreground()
    }

    element(NeuImeUi.ClipboardHeader) {
        background()
        foreground()
        font()
    }
    element(NeuImeUi.ClipboardItem) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.ClipboardItemPopup) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.ClipboardEnableHistoryButton) {
        background()
        foreground()
        shape()
    }

    element(NeuImeUi.EmojiKey) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.EmojiKeyPopup) {
        background()
        foreground()
        font()
        shape()
        shadow()
        border()
    }
    element(NeuImeUi.EmojiTab) {
        foreground()
    }

    element(NeuImeUi.ExtractedLandscapeInputLayout) {
        background()
    }
    element(NeuImeUi.ExtractedLandscapeInputField) {
        background()
        foreground()
        font()
        shape()
        border()
    }
    element(NeuImeUi.ExtractedLandscapeInputAction) {
        background()
        foreground()
        shape()
    }

    element(NeuImeUi.GlideTrail) {
        foreground()
    }

    element(NeuImeUi.IncognitoModeIndicator) {
        foreground()
    }

    element(NeuImeUi.OneHandedPanel) {
        background()
        foreground()
    }

    element(NeuImeUi.SystemNavBar) {
        background()
    }
})

Snygg.init(
            stylesheetSpec = NeuImeUiSpec,
            rulePreferredElementSorting = listOf(
                NeuImeUi.Keyboard,
                NeuImeUi.Key,
                NeuImeUi.KeyHint,
                NeuImeUi.KeyPopup,
                NeuImeUi.Smartbar,
                NeuImeUi.SmartbarSharedActionsRow,
                NeuImeUi.SmartbarSharedActionsToggle,
                NeuImeUi.SmartbarExtendedActionsRow,
                NeuImeUi.SmartbarExtendedActionsToggle,
                NeuImeUi.SmartbarActionKey,
                NeuImeUi.SmartbarActionTile,
                NeuImeUi.SmartbarActionsOverflow,
                NeuImeUi.SmartbarActionsOverflowCustomizeButton,
                NeuImeUi.SmartbarActionsEditor,
                NeuImeUi.SmartbarActionsEditorHeader,
                NeuImeUi.SmartbarActionsEditorSubheader,
                NeuImeUi.SmartbarCandidatesRow,
                NeuImeUi.SmartbarCandidateWord,
                NeuImeUi.SmartbarCandidateClip,
                NeuImeUi.SmartbarCandidateSpacer,
            ),
            rulePlaceholders = mapOf(
                "c:delete" to KeyCode.DELETE,
                "c:enter" to KeyCode.ENTER,
                "c:shift" to KeyCode.SHIFT,
                "c:space" to KeyCode.SPACE,
                "sh:unshifted" to InputShiftState.UNSHIFTED.value,
                "sh:shifted_manual" to InputShiftState.SHIFTED_MANUAL.value,
                "sh:shifted_automatic" to InputShiftState.SHIFTED_AUTOMATIC.value,
                "sh:caps_lock" to InputShiftState.CAPS_LOCK.value,
            ),
        )
*/
