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

package dev.patrickgold.neuboard.app.settings.typing

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.lib.compose.FlorisCanvasIcon
import dev.patrickgold.neuboard.lib.compose.NeuboardErrorCard
import dev.patrickgold.neuboard.lib.compose.NeuboardSimpleCard
import dev.patrickgold.neuboard.lib.compose.NeuboardWarningCard
import dev.patrickgold.neuboard.lib.compose.observeAsState
import dev.patrickgold.neuboard.lib.compose.stringRes
import dev.patrickgold.neuboard.lib.util.launchActivity
import org.neuboard.lib.android.AndroidSettings

@Composable
fun SpellCheckerServiceSelector(florisSpellCheckerEnabled: MutableState<Boolean>) {
    val context = LocalContext.current

    val systemSpellCheckerId by AndroidSettings.Secure.observeAsState(
        key = "selected_spell_checker",
        foregroundOnly = true,
    )
    val systemSpellCheckerEnabled by AndroidSettings.Secure.observeAsState(
        key = "spell_checker_enabled",
        foregroundOnly = true,
    )
    val systemSpellCheckerPkgName = remember(systemSpellCheckerId) {
        runCatching {
            ComponentName.unflattenFromString(systemSpellCheckerId!!)!!.packageName
        }.getOrDefault("null")
    }
    val openSystemSpellCheckerSettings = {
        val componentToLaunch = ComponentName(
            "com.android.settings",
            "com.android.settings.Settings\$SpellCheckersSettingsActivity",
        )
        context.launchActivity {
            it.addCategory(Intent.CATEGORY_DEFAULT)
            it.component = componentToLaunch
        }
    }
    florisSpellCheckerEnabled.value =
        systemSpellCheckerEnabled == "1" &&
        systemSpellCheckerPkgName == context.packageName

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        if (systemSpellCheckerEnabled == "1") {
            if (systemSpellCheckerId == null) {
                NeuboardWarningCard(
                    text = stringRes(R.string.pref__spelling__active_spellchecker__summary_none),
                    onClick = openSystemSpellCheckerSettings,
                )
            } else {
                var spellCheckerIcon: Drawable?
                var spellCheckerLabel = "Unknown"
                try {
                    val pm = context.packageManager
                    val remoteAppInfo = pm.getApplicationInfo(systemSpellCheckerPkgName, 0)
                    spellCheckerIcon = pm.getApplicationIcon(remoteAppInfo)
                    spellCheckerLabel = pm.getApplicationLabel(remoteAppInfo).toString()
                } catch (e: Exception) {
                    spellCheckerIcon = null
                }
                NeuboardSimpleCard(
                    icon = {
                        if (spellCheckerIcon != null) {
                            FlorisCanvasIcon(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .requiredSize(32.dp),
                                drawable = spellCheckerIcon,
                            )
                        } else {
                            Icon(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .requiredSize(32.dp),
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                            )
                        }
                    },
                    text = spellCheckerLabel,
                    secondaryText = systemSpellCheckerPkgName,
                    contentPadding = PaddingValues(all = 8.dp),
                    onClick = openSystemSpellCheckerSettings,
                )
            }
        } else {
            NeuboardErrorCard(
                text = stringRes(R.string.pref__spelling__active_spellchecker__summary_disabled),
                onClick = openSystemSpellCheckerSettings,
            )
        }
    }
}
