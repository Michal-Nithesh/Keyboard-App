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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.patrickgold.neuboard.BuildConfig
import dev.patrickgold.neuboard.R
import dev.patrickgold.neuboard.app.LocalNavController
import dev.patrickgold.neuboard.app.Routes
import dev.patrickgold.neuboard.lib.compose.NeuboardOutlinedBox
import dev.patrickgold.neuboard.lib.compose.FlorisTextButton
import dev.patrickgold.neuboard.lib.compose.defaultNeuboardOutlinedBox
import dev.patrickgold.neuboard.lib.compose.stringRes
import dev.patrickgold.neuboard.lib.ext.Extension
import dev.patrickgold.neuboard.lib.ext.generateUpdateUrl
import dev.patrickgold.neuboard.lib.util.launchUrl
import org.neuboard.lib.kotlin.curlyFormat

@Composable
fun ImportExtensionBox(navController: NavController) {
    val context = LocalContext.current
    NeuboardOutlinedBox(
        modifier = Modifier.defaultNeuboardOutlinedBox(),
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
            text = stringRes(id = R.string.ext__home__info),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
        ) {
            FlorisTextButton(
                onClick = {
                    context.launchUrl("https://${BuildConfig.FLADDONS_STORE_URL}/")
                },
                icon = Icons.Default.Shop,
                text = stringRes(id = R.string.ext__home__visit_store),
            )
            Spacer(modifier = Modifier.weight(1f))
            FlorisTextButton(
                onClick = {
                    navController.navigate(Routes.Ext.Import(ExtensionImportScreenType.EXT_ANY, null))
                },
                icon = Icons.AutoMirrored.Filled.Input,
                text = stringRes(R.string.action__import),
            )
        }
    }
}

@Composable
fun UpdateBox(extensionIndex: List<Extension>) {
    val context = LocalContext.current
    NeuboardOutlinedBox(
        modifier = Modifier.defaultNeuboardOutlinedBox(),
    ) {
        Text(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
            text = stringRes(id = R.string.ext__update_box__internet_permission_hint),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
        ) {
            FlorisTextButton(
                onClick = {
                    context.launchUrl(extensionIndex.generateUpdateUrl())
                },
                icon = Icons.Outlined.FileDownload,
                text = stringRes(id = R.string.ext__update_box__search_for_updates)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AddonManagementReferenceBox(
    type: ExtensionListScreenType
) {
    val navController = LocalNavController.current

    NeuboardOutlinedBox(
        modifier = Modifier.defaultNeuboardOutlinedBox(),
        title = stringRes(id = R.string.ext__addon_management_box__managing_placeholder).curlyFormat(
            "extensions" to type.let { stringRes(id = it.titleResId).lowercase() }
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            text = stringRes(id = R.string.ext__addon_management_box__addon_manager_info),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            FlorisTextButton(
                onClick = {
                    val route = Routes.Ext.List(type, showUpdate = true)
                    navController.navigate(
                        route
                    )
                },
                icon = Icons.Default.Shop,
                text = stringRes(id = R.string.ext__addon_management_box__go_to_page).curlyFormat(
                    "ext_home_title" to stringRes(type.titleResId),
                ),
            )
        }
    }
}
