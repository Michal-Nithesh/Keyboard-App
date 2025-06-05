/*
 * Copyright (C) 2025 The NeuBoard Contributors
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

package dev.patrickgold.neuboard.app.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.extensionManager
import dev.patrickgold.neuboard.lib.compose.NeuScreen
import dev.patrickgold.neuboard.lib.compose.stringRes

@Composable
fun CheckUpdatesScreen() = NeuScreen {
    title = stringRes(R.string.ext__check_updates__title)

    val context = LocalContext.current
    val extensionManager by context.extensionManager()
    val extensionIndex = extensionManager.combinedExtensionList()

    content {
        UpdateBox(extensionIndex)
    }
}
