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

package dev.patrickgold.neuboard.app.settings.dictionary

import androidx.compose.runtime.Composable
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.app.LocalNavController
import dev.patrickgold.neuboard.app.Routes
import dev.patrickgold.neuboard.lib.compose.NeuboardScreen
import dev.patrickgold.neuboard.lib.compose.stringRes
import dev.patrickgold.jetpref.datastore.ui.Preference
import dev.patrickgold.jetpref.datastore.ui.SwitchPreference

@Composable
fun DictionaryScreen() = NeuboardScreen {
    title = stringRes(R.string.settings__dictionary__title)
    previewFieldVisible = true

    val navController = LocalNavController.current

    content {
        SwitchPreference(
            prefs.dictionary.enableSystemUserDictionary,
            title = stringRes(R.string.pref__dictionary__enable_system_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__enable_system_user_dictionary__summary),
        )
        Preference(
            title = stringRes(R.string.pref__dictionary__manage_system_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__manage_system_user_dictionary__summary),
            onClick = { navController.navigate(Routes.Settings.UserDictionary(UserDictionaryType.SYSTEM)) },
            enabledIf = { prefs.dictionary.enableSystemUserDictionary isEqualTo true },
        )
        SwitchPreference(
            prefs.dictionary.enableNeuboardUserDictionary,
            title = stringRes(R.string.pref__dictionary__enable_internal_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__enable_internal_user_dictionary__summary),
        )
        Preference(
            title = stringRes(R.string.pref__dictionary__manage_floris_user_dictionary__label),
            summary = stringRes(R.string.pref__dictionary__manage_floris_user_dictionary__summary),
            onClick = { navController.navigate(Routes.Settings.UserDictionary(UserDictionaryType.NEU)) },
            enabledIf = { prefs.dictionary.enableNeuboardUserDictionary isEqualTo true },
        )
    }
}
