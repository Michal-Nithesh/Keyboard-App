/*
 * Copyright (C) 2022-2025 The NeuBoard Contributors
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

package dev.patrickgold.neuboard.ime.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.patrickgold.neuboard.aiEnhancementManager
import dev.patrickgold.neuboard.editorInstance
import dev.patrickgold.neuboard.ime.theme.NeuboardImeUi
import dev.patrickgold.neuboard.lib.compose.conditional
import dev.patrickgold.neuboard.lib.compose.florisHorizontalScroll
import org.neuboard.lib.snygg.ui.SnyggRow
import org.neuboard.lib.snygg.ui.SnyggText

@Composable
fun QuickReplySuggestionsRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val aiEnhancementManager by context.aiEnhancementManager()
    val editorInstance by context.editorInstance()
    
    val suggestions by aiEnhancementManager.quickReplySuggestions.collectAsState()
    if (suggestions.isEmpty()) {
        return
    }
    
    SnyggRow(
        elementName = NeuboardImeUi.SmartbarCandidatesRow.elementName,
        modifier = modifier
            .fillMaxSize()
            .conditional(suggestions.size > 1) {
                florisHorizontalScroll(scrollbarHeight = 2.dp)
            },
        horizontalArrangement = Arrangement.Start,
    ) {
        // Use a non-scrollable row layout instead of LazyRow to avoid nested scrollables
        // LazyRow was causing "Horizontally scrollable component was measured with an infinity maximum width constraints" error
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .wrapContentWidth()
                        .clickable { editorInstance.commitText(suggestion) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 2.dp
                ) {
                    SnyggText(
                        elementName = NeuboardImeUi.SmartbarCandidateWord.elementName,
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
