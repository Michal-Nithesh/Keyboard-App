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

package org.neuboard.lib.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectIn(scope: CoroutineScope, collector: FlowCollector<T>) {
    scope.launch { this@collectIn.collect(collector) }
}

fun <T> Flow<T>.collectLatestIn(scope: CoroutineScope, action: suspend (value: T) -> Unit) {
    scope.launch { this@collectLatestIn.collectLatest(action) }
}
