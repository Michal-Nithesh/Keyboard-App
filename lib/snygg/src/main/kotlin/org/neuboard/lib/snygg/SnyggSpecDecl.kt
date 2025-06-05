/*
 * Copyright (C) 2025 The Neuboard Contributors
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

package org.neuboard.lib.snygg

import org.neuboard.lib.snygg.value.SnyggValueEncoder
import org.neuboard.lib.snygg.PropertySet
import org.neuboard.lib.snygg.Property

enum class InheritBehavior {
    IMPLICITLY_OR_EXPLICITLY,
    EXPLICITLY_ONLY,
}

open class SnyggSpecDecl(configure: SnyggSpecDeclBuilder.() -> Unit) {
    internal val annotationSpecs: Map<RuleDecl, PropertySet>
    internal val elementsSpec: PropertySet
    internal val meta: JsonSchemaMeta
    private val _allEncoders = mutableSetOf<SnyggValueEncoder>()
    internal val allEncoders: Set<SnyggValueEncoder>
        get() = _allEncoders

    init {
        val builder = SnyggSpecDeclBuilder()
        builder.configure()
        val (annotationSpecsB, elementsSpecB, metaB) = builder.build()
        annotationSpecs = annotationSpecsB
        elementsSpec = elementsSpecB
        meta = metaB
    }

    fun propertySetSpecOf(rule: SnyggRule): PropertySet? {
        val propertySetSpec = when (rule) {
            is SnyggAnnotationRule -> annotationSpecs[rule.decl()]
            is SnyggElementRule -> elementsSpec
        } ?: return null
        return propertySetSpec
    }

    fun propertiesOf(rule: SnyggRule): Set<String> {
        val propertySetSpec = propertySetSpecOf(rule)
        return propertySetSpec?.properties?.keys.orEmpty()
    }

    fun encodersOf(rule: SnyggRule, property: String): Set<SnyggValueEncoder>? {
        val propertySetSpec = propertySetSpecOf(rule) ?: return null
        val encoders = propertySetSpec.properties[property]?.encoders
        if (encoders != null) {
            return encoders
        }
        for ((regex, patternPropertySetSpec) in propertySetSpec.patternProperties) {
            if (regex.matchEntire(property) != null) {
                return patternPropertySetSpec.encoders
            }
        }
        return null
    }

    interface RuleDecl {
        val name: String
        val pattern: Regex
    }

    // --- Public top-level SnyggSpecDeclBuilder ---
    public class SnyggSpecDeclBuilder(
        private val _allEncoders: MutableSet<SnyggValueEncoder> = mutableSetOf()
    ) {
        private val annotationSpecs = mutableMapOf<SnyggSpecDecl.RuleDecl, PropertySetBuilder>()
        private val elementsSpec = PropertySetBuilder(type = SnyggSpecDecl.PropertySet.Type.SINGLE_SET, _allEncoders)
        val meta = JsonSchemaMetaBuilder()

        fun annotation(ruleDecl: SnyggSpecDecl.RuleDecl) = SingleOrMultiple(ruleDecl, annotationSpecs, _allEncoders)

        fun elements(configure: PropertySetBuilder.() -> Unit) {
            elementsSpec.configure()
        }

        fun build(): Triple<Map<SnyggSpecDecl.RuleDecl, SnyggSpecDecl.PropertySet>, SnyggSpecDecl.PropertySet, JsonSchemaMeta> {
            val annotationSpecs = annotationSpecs.mapValues { (_, builder) ->
                builder.build()
            }
            val elementsSpec = elementsSpec.build()
            return Triple(annotationSpecs, elementsSpec, meta.build())
        }
    }

    public class SingleOrMultiple(
        private val ruleDecl: SnyggSpecDecl.RuleDecl,
        private val annotationSpecs: MutableMap<SnyggSpecDecl.RuleDecl, PropertySetBuilder>,
        private val _allEncoders: MutableSet<SnyggValueEncoder>
    ) {
        init {
            check(!annotationSpecs.containsKey(ruleDecl)) {
                "Duplicate definition of $ruleDecl"
            }
        }

        fun singleSet(configure: PropertySetBuilder.() -> Unit) {
            val annotationSpec = PropertySetBuilder(type = SnyggSpecDecl.PropertySet.Type.SINGLE_SET, _allEncoders)
            annotationSpec.configure()
            annotationSpecs[ruleDecl] = annotationSpec
        }

        fun multipleSets(configure: PropertySetBuilder.() -> Unit) {
            val annotationSpec = PropertySetBuilder(type = SnyggSpecDecl.PropertySet.Type.MULTIPLE_SETS, _allEncoders)
            annotationSpec.configure()
            annotationSpecs[ruleDecl] = annotationSpec
        }
    }

    public class PropertySetBuilder(
        private val type: SnyggSpecDecl.PropertySet.Type,
        private val _allEncoders: MutableSet<SnyggValueEncoder> = mutableSetOf()
    ) {
        private val patternProperties = mutableMapOf<Regex, PropertyBuilder>()
        private val properties = mutableMapOf<String, PropertyBuilder>()
        val meta = JsonSchemaMetaBuilder()
        private val implicitEncoders = mutableSetOf<SnyggValueEncoder>()

        fun pattern(regex: Regex, configure: PropertyBuilder.() -> Unit) {
            val builder = patternProperties.getOrDefault(regex, PropertyBuilder(_allEncoders))
            builder.configure()
            patternProperties[regex] = builder
        }

        operator fun String.invoke(configure: PropertyBuilder.() -> Unit) {
            val builder = properties.getOrDefault(this, PropertyBuilder(_allEncoders))
            builder.configure()
            properties[this] = builder
        }

        fun implicit(configure: MutableSet<SnyggValueEncoder>.() -> Unit) {
            implicitEncoders.configure()
        }

        fun build(): SnyggSpecDecl.PropertySet {
            _allEncoders.addAll(implicitEncoders)
            val patternPropertySpecs = patternProperties.mapValues { (_, builder) ->
                builder.build(implicitEncoders)
            }
            val propertySpecs = properties.mapValues { (_, builder) ->
                builder.build(implicitEncoders)
            }
            return SnyggSpecDecl.PropertySet(
                type = type,
                patternProperties = patternPropertySpecs,
                properties = propertySpecs,
                meta = meta.build(),
            )
        }
    }

    public class PropertyBuilder(
        private val _allEncoders: MutableSet<SnyggValueEncoder> = mutableSetOf()
    ) {
        private val encoders: MutableSet<SnyggValueEncoder> = mutableSetOf()
        private var inheritBehavior: InheritBehavior = InheritBehavior.EXPLICITLY_ONLY
        private var required = false
        val meta = JsonSchemaMetaBuilder()
        private var isAny: Boolean = false

        fun add(encoder: SnyggValueEncoder) {
            encoders.add(encoder)
            _allEncoders.add(encoder)
        }

        fun any() {
            isAny = true
        }

        fun inheritsImplicitly() {
            inheritBehavior = InheritBehavior.IMPLICITLY_OR_EXPLICITLY
        }

        fun required() {
            required = true
        }

        fun build(implicitEncoders: Set<SnyggValueEncoder>): SnyggSpecDecl.Property {
            val encoders = if (isAny) {
                _allEncoders
            } else {
                buildSet {
                    addAll(implicitEncoders)
                    addAll(this@PropertyBuilder.encoders)
                }
            }
            return SnyggSpecDecl.Property(encoders, inheritBehavior, required, meta.build())
        }
    }
}

// --- Public top-level PropertySet and Property ---
public data class PropertySet(
    val type: PropertySet.Type,
    val patternProperties: Map<Regex, Property>,
    val properties: Map<String, Property>,
    val meta: JsonSchemaMeta,
) {
    public enum class Type {
        SINGLE_SET,
        MULTIPLE_SETS;
    }
}

public data class Property(
    val encoders: Set<SnyggValueEncoder>,
    val inheritBehavior: InheritBehavior,
    val required: Boolean,
    val meta: JsonSchemaMeta,
) {
    fun inheritsImplicitly(): Boolean {
        return inheritBehavior == InheritBehavior.IMPLICITLY_OR_EXPLICITLY
    }
}

data class JsonSchemaMeta(
    val title: String,
    val description: String,
)

class JsonSchemaMetaBuilder(
    var title: String = "",
    var description: String = "",
) {
    fun build(): JsonSchemaMeta {
        return JsonSchemaMeta(title, description)
    }
}
