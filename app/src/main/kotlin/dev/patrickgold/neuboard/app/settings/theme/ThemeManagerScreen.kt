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

package dev.patrickgold.neuboard.app.settings.theme

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.app.neuboardPreferenceModel
import dev.patrickgold.neuboard.extensionManager
import dev.patrickgold.neuboard.ime.theme.ThemeExtensionComponent
import dev.patrickgold.neuboard.lib.compose.NeuboardOutlinedBox
import dev.patrickgold.neuboard.lib.compose.NeuboardScreen
import dev.patrickgold.neuboard.lib.compose.defaultNeuboardOutlinedBox
import dev.patrickgold.neuboard.lib.compose.rippleClickable
import dev.patrickgold.neuboard.lib.compose.stringRes
import dev.patrickgold.neuboard.lib.ext.ExtensionComponentName
import dev.patrickgold.neuboard.lib.observeAsNonNullState
import dev.patrickgold.neuboard.themeManager
import dev.patrickgold.jetpref.datastore.model.observeAsState
import dev.patrickgold.jetpref.material.ui.JetPrefListItem

enum class ThemeManagerScreenAction(val id: String) {
    SELECT_DAY("select-day"),
    SELECT_NIGHT("select-night");
}

@Composable
fun ThemeManagerScreen(action: ThemeManagerScreenAction?) = NeuboardScreen {
    title = stringRes(when (action) {
        ThemeManagerScreenAction.SELECT_DAY -> R.string.settings__theme_manager__title_day
        ThemeManagerScreenAction.SELECT_NIGHT -> R.string.settings__theme_manager__title_night
        else -> error("Theme manager screen action must not be null")
    })
    previewFieldVisible = true

    val prefs by neuboardPreferenceModel()
    val context = LocalContext.current
    val extensionManager by context.extensionManager()
    val themeManager by context.themeManager()

    val indexedThemeExtensions by extensionManager.themes.observeAsNonNullState()
    val extGroupedThemes = remember(indexedThemeExtensions) {
        buildMap<String, List<ThemeExtensionComponent>> {
            for (ext in indexedThemeExtensions) {
                put(ext.meta.id, ext.themes)
            }
        }.mapValues { (_, configs) -> configs.sortedBy { it.label } }
    }

    fun getThemeIdPref() = when (action) {
        ThemeManagerScreenAction.SELECT_DAY -> prefs.theme.dayThemeId
        ThemeManagerScreenAction.SELECT_NIGHT -> prefs.theme.nightThemeId
    }

    fun setTheme(extId: String, componentId: String) {
        val extComponentName = ExtensionComponentName(extId, componentId)
        when (action) {
            ThemeManagerScreenAction.SELECT_DAY,
            ThemeManagerScreenAction.SELECT_NIGHT -> {
                getThemeIdPref().set(extComponentName)
            }
        }
    }

    val activeThemeId by when (action) {
        ThemeManagerScreenAction.SELECT_DAY,
        ThemeManagerScreenAction.SELECT_NIGHT -> getThemeIdPref().observeAsState()
    }

    content {
        DisposableEffect(activeThemeId) {
            themeManager.previewThemeId = activeThemeId
            onDispose {
                themeManager.previewThemeId = null
            }
        }
        val grayColor = LocalContentColor.current.copy(alpha = 0.56f)
        for ((extensionId, configs) in extGroupedThemes) key(extensionId) {
            val ext = extensionManager.getExtensionById(extensionId)!!
            NeuboardOutlinedBox(
                modifier = Modifier.defaultNeuboardOutlinedBox(),
                title = ext.meta.title,
                subtitle = extensionId,
            ) {
                for (config in configs) key(extensionId, config.id) {
                    JetPrefListItem(
                        modifier = Modifier.rippleClickable {
                            setTheme(extensionId, config.id)
                        },
                        icon = {
                            RadioButton(
                                selected = activeThemeId.extensionId == extensionId &&
                                    activeThemeId.componentId == config.id,
                                onClick = null,
                            )
                        },
                        text = config.label,
                        trailing = {
                            Icon(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                imageVector = if (config.isNightTheme) {
                                    Icons.Default.DarkMode
                                } else {
                                    Icons.Default.LightMode
                                },
                                contentDescription = null,
                                tint = grayColor,
                            )
                        },
                    )
                }
            }
        }
    }
}
