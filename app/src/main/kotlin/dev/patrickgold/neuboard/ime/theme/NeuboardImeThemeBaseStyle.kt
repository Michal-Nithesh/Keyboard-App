/*
 * Copyright (C) 2022-2025 The Neuboard Contributors
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

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.patrickgold.neuboard.ime.input.InputShiftState
import dev.patrickgold.neuboard.ime.text.key.KeyCode
import org.neuboard.lib.snygg.SnyggSelector
import org.neuboard.lib.snygg.SnyggStylesheet
import org.neuboard.lib.snygg.value.circleShape
import org.neuboard.lib.snygg.value.fontSize
import org.neuboard.lib.snygg.value.genericFontFamily
import org.neuboard.lib.snygg.value.rgbaColor
import org.neuboard.lib.snygg.value.rectangleShape
import org.neuboard.lib.snygg.value.roundedCornerShape
import org.neuboard.lib.snygg.value.size
import org.neuboard.lib.snygg.value.textAlign
import org.neuboard.lib.snygg.value.textMaxLines
import org.neuboard.lib.snygg.value.textOverflow
import org.neuboard.lib.snygg.value.fontWeight

val NeuboardImeThemeBaseStyle = SnyggStylesheet.v2 {
    defines {
        "--primary" to rgbaColor(76, 175, 80)
        "--primary-variant" to rgbaColor(56, 142, 60)
        "--secondary" to rgbaColor(245, 124, 0)
        "--secondary-variant" to rgbaColor(230, 81, 0)
        "--background" to rgbaColor(33, 33, 33)
        "--background-variant" to rgbaColor(44, 44, 44)
        "--surface" to rgbaColor(66, 66, 66)
        "--surface-variant" to rgbaColor(97, 97, 97)

        "--on-primary" to rgbaColor(240, 240, 240)
        "--on-background" to rgbaColor(255, 255, 255)
        "--on-background-disabled" to rgbaColor(80, 80, 80)
        "--on-surface" to rgbaColor(255, 255, 255)

        "--shape" to roundedCornerShape(8.dp)
        "--shape-variant" to roundedCornerShape(12.dp)
    }

    NeuboardImeUi.Window.elementName {
        background = `var`("--background")
        foreground = `var`("--on-background")
    }

    NeuboardImeUi.Key.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        fontSize = fontSize(22.sp)
        shadowElevation = size(2.dp)
        shape = `var`("--shape")
        textMaxLines = textMaxLines(1)
    }
    NeuboardImeUi.Key.elementName(selector = SnyggSelector.PRESSED) {
        background = `var`("--surface-variant")
        foreground = `var`("--on-surface")
    }
    NeuboardImeUi.Key.elementName(NeuboardImeUi.Attr.Code to listOf(KeyCode.ENTER)) {
        background = `var`("--primary")
        foreground = `var`("--on-surface")
    }
    NeuboardImeUi.Key.elementName(NeuboardImeUi.Attr.Code to listOf(KeyCode.ENTER), selector = SnyggSelector.PRESSED) {
        background = `var`("--primary-variant")
        foreground = `var`("--on-surface")
    }
    NeuboardImeUi.Key.elementName(NeuboardImeUi.Attr.Code to listOf(KeyCode.SPACE)) {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        fontSize = fontSize(12.sp)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.Key.elementName(NeuboardImeUi.Attr.Code to listOf(
        KeyCode.VIEW_CHARACTERS,
        KeyCode.VIEW_SYMBOLS,
        KeyCode.VIEW_SYMBOLS2,
    )) {
        fontSize = fontSize(18.sp)
    }
    NeuboardImeUi.Key.elementName(NeuboardImeUi.Attr.Code to listOf(
        KeyCode.VIEW_NUMERIC,
        KeyCode.VIEW_NUMERIC_ADVANCED,
    )) {
        fontSize = fontSize(12.sp)
    }
    NeuboardImeUi.Key.elementName(NeuboardImeUi.Attr.Code to listOf(KeyCode.VIEW_NUMERIC_ADVANCED)) {
        textMaxLines = textMaxLines(2)
    }
    NeuboardImeUi.Key.elementName(
        NeuboardImeUi.Attr.Code to listOf(KeyCode.SHIFT),
        NeuboardImeUi.Attr.ShiftState to listOf(InputShiftState.CAPS_LOCK.toString()),
    ) {
        foreground = rgbaColor(255, 152, 0)
    }
    NeuboardImeUi.KeyHint.elementName {
        background = rgbaColor(0, 0, 0, 0f)
        foreground = `var`("--on-surface-variant")
        fontFamily = genericFontFamily(FontFamily.Monospace)
        fontSize = fontSize(12.sp)
        padding = padding(0.dp, 1.dp, 1.dp, 0.dp)
        textMaxLines = textMaxLines(1)
    }
    NeuboardImeUi.KeyPopupBox.elementName {
        background = rgbaColor(117, 117, 117)
        foreground = `var`("--on-surface")
        fontSize = fontSize(22.sp)
        shape = `var`("--shape")
        shadowElevation = size(2.dp)
    }
    NeuboardImeUi.KeyPopupElement.elementName(selector = SnyggSelector.FOCUS) {
        background = rgbaColor(189, 189, 189)
        shape = `var`("--shape")
    }
    NeuboardImeUi.KeyPopupExtendedIndicator.elementName {
        fontSize = fontSize(16.sp)
    }

    NeuboardImeUi.Smartbar.elementName {
        fontSize = fontSize(18.sp)
    }
    NeuboardImeUi.SmartbarSharedActionsToggle.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        margin = padding(6.dp)
        shape = circleShape()
        shadowElevation = size(2.dp)
    }
    NeuboardImeUi.SmartbarExtendedActionsToggle.elementName {
        background = rgbaColor(0, 0, 0, 0f)
        foreground = rgbaColor(144, 144, 144)
        margin = padding(6.dp)
        shape = circleShape()
    }
    NeuboardImeUi.SmartbarActionKey.elementName {
        background = rgbaColor(0, 0, 0, 0f)
        foreground = rgbaColor(220, 220, 220)
        shape = `var`("--shape")
    }
    NeuboardImeUi.SmartbarActionKey.elementName(selector = SnyggSelector.DISABLED) {
        foreground = `var`("--on-background-disabled")
    }

    NeuboardImeUi.SmartbarActionsOverflow.elementName {
        margin = padding(4.dp)
    }
    NeuboardImeUi.SmartbarActionsOverflowCustomizeButton.elementName {
        background = `var`("--primary")
        foreground = `var`("--on-primary")
        fontSize = fontSize(14.sp)
        margin = padding(0.dp, 8.dp, 0.dp, 0.dp)
        shape = roundedCornerShape(24.dp)
    }
    NeuboardImeUi.SmartbarActionTile.elementName {
        background = `var`("--background-variant")
        foreground = `var`("--on-background")
        fontSize = fontSize(14.sp)
        margin = padding(4.dp)
        padding = padding(4.dp)
        shape = roundedCornerShape(20)
        textAlign = textAlign(TextAlign.Center)
        textMaxLines = textMaxLines(2)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SmartbarActionTile.elementName(selector = SnyggSelector.DISABLED) {
        foreground = `var`("--on-background-disabled")
    }
    NeuboardImeUi.SmartbarActionTileIcon.elementName {
        fontSize = fontSize(24.sp)
        margin = padding(0.dp, 0.dp, 0.dp, 8.dp)
    }

    NeuboardImeUi.SmartbarActionsEditor.elementName {
        background = `var`("--background")
        foreground = `var`("--on-background")
        shape = roundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
    }
    NeuboardImeUi.SmartbarActionsEditorHeader.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        fontSize = fontSize(16.sp)
        textMaxLines = textMaxLines(1)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SmartbarActionsEditorHeaderButton.elementName {
        margin = padding(4.dp)
        shape = circleShape()
    }
    NeuboardImeUi.SmartbarActionsEditorSubheader.elementName {
        foreground = `var`("--secondary")
        fontSize = fontSize(16.sp)
        fontWeight = fontWeight(FontWeight.Bold)
        padding = padding(12.dp, 16.dp, 12.dp, 8.dp)
        textMaxLines = textMaxLines(1)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SmartbarActionsEditorTileGrid.elementName {
        margin = padding(4.dp, 0.dp)
    }
    NeuboardImeUi.SmartbarActionsEditorTile.elementName {
        margin = padding(4.dp)
        padding = padding(8.dp)
        textAlign = textAlign(TextAlign.Center)
        textMaxLines = textMaxLines(2)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SmartbarActionsEditorTile.elementName(NeuboardImeUi.Attr.Code to listOf(KeyCode.NOOP)) {
        foreground = `var`("--on-background-disabled")
    }
    NeuboardImeUi.SmartbarActionsEditorTile.elementName(NeuboardImeUi.Attr.Code to listOf(KeyCode.DRAG_MARKER)) {
        foreground = rgbaColor(255, 0, 0)
    }

    NeuboardImeUi.SmartbarCandidateWord.elementName {
        background = rgbaColor(0, 0, 0, 0f)
        foreground = `var`("--on-background")
        fontSize = fontSize(14.sp)
        margin = padding(4.dp)
        padding = padding(8.dp, 0.dp)
        shape = rectangleShape()
        textMaxLines = textMaxLines(1)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SmartbarCandidateWord.elementName(selector = SnyggSelector.PRESSED) {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
    }
    NeuboardImeUi.SmartbarCandidateWordSecondaryText.elementName {
        fontSize = fontSize(8.sp)
        margin = padding(0.dp, 2.dp, 0.dp, 0.dp)
    }
    NeuboardImeUi.SmartbarCandidateClip.elementName {
        background = rgbaColor(0, 0, 0, 0f)
        foreground = rgbaColor(220, 220, 220)
        fontSize = fontSize(14.sp)
        margin = padding(4.dp)
        padding = padding(8.dp, 0.dp)
        shape = roundedCornerShape(8)
        textMaxLines = textMaxLines(1)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SmartbarCandidateClip.elementName(selector = SnyggSelector.PRESSED) {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
    }
    NeuboardImeUi.SmartbarCandidateClipIcon.elementName {
        margin = padding(0.dp, 0.dp, 4.dp, 0.dp)
    }
    NeuboardImeUi.SmartbarCandidateSpacer.elementName {
        foreground = rgbaColor(255, 255, 255, 0.25f)
    }

    NeuboardImeUi.ClipboardHeader.elementName {
        foreground = `var`("--on-background")
        fontSize = fontSize(16.sp)
    }
    NeuboardImeUi.ClipboardSubheader.elementName {
        fontSize = fontSize(14.sp)
        margin = padding(6.dp)
    }
    NeuboardImeUi.ClipboardContent.elementName {
        padding = padding(10.dp)
    }
    NeuboardImeUi.ClipboardItem.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        fontSize = fontSize(14.sp)
        margin = padding(4.dp)
        padding = padding(12.dp, 8.dp)
        shape = `var`("--shape-variant")
        shadowElevation = size(2.dp)
        textMaxLines = textMaxLines(10)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.ClipboardItemPopup.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        fontSize = fontSize(14.sp)
        margin = padding(4.dp)
        padding = padding(12.dp, 8.dp)
        shape = `var`("--shape-variant")
        shadowElevation = size(2.dp)
    }
    NeuboardImeUi.ClipboardItemActions.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        margin = padding(4.dp)
        shape = `var`("--shape-variant")
        shadowElevation = size(2.dp)
    }
    NeuboardImeUi.ClipboardItemAction.elementName {
        fontSize = fontSize(16.sp)
        padding = padding(12.dp)
    }
    NeuboardImeUi.ClipboardItemActionText.elementName {
        margin = padding(8.dp, 0.dp, 0.dp, 0.dp)
    }
    NeuboardImeUi.ClipboardHistoryDisabledButton.elementName {
        background = `var`("--primary")
        foreground = `var`("--on-primary")
        shape = roundedCornerShape(24.dp)
    }

    NeuboardImeUi.MediaEmojiKey.elementName {
        background = rgbaColor(0, 0, 0, 0f)
        foreground = `var`("--on-background")
        fontSize = fontSize(22.sp)
        shape = `var`("--shape")
    }
    NeuboardImeUi.MediaEmojiKey.elementName(selector = SnyggSelector.PRESSED) {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
    }

    NeuboardImeUi.GlideTrail.elementName {
        foreground = `var`("--primary")
    }

    NeuboardImeUi.InlineAutofillChip.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
    }

    NeuboardImeUi.IncognitoModeIndicator.elementName {
        foreground = rgbaColor(255, 255, 255, 0.067f)
    }

    NeuboardImeUi.OneHandedPanel.elementName {
        background = rgbaColor(27, 94, 32)
        foreground = rgbaColor(238, 238, 238)
    }

    NeuboardImeUi.SubtypePanel.elementName {
        background = `var`("--background")
        foreground = `var`("--on-background")
        shape = roundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
    }
    NeuboardImeUi.SubtypePanelHeader.elementName {
        background = `var`("--surface")
        foreground = `var`("--on-surface")
        fontSize = fontSize(18.sp)
        padding = padding(12.dp)
        textAlign = textAlign(TextAlign.Center)
        textMaxLines = textMaxLines(1)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
    NeuboardImeUi.SubtypePanelListItem.elementName {
        fontSize = fontSize(16.sp)
        padding = padding(16.dp)
    }
    NeuboardImeUi.SubtypePanelListItemIconLeading.elementName {
        fontSize = fontSize(24.sp)
        padding = padding(0.dp, 0.dp, 16.dp, 0.dp)
    }
    NeuboardImeUi.SubtypePanelListItemText.elementName {
        textMaxLines = textMaxLines(1)
        textOverflow = textOverflow(TextOverflow.Ellipsis)
    }
}
