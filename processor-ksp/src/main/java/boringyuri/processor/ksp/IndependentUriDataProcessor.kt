/*
 * Copyright 2022 Anton Novikau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boringyuri.processor.ksp

import boringyuri.api.DefaultValue
import boringyuri.api.Param
import boringyuri.api.Path
import boringyuri.api.UriData
import boringyuri.api.adapter.TypeAdapter
import boringyuri.processor.common.base.BoringProcessingStep
import boringyuri.processor.common.base.ProcessingSession
import boringyuri.processor.common.ksp.KspBoringAnnotationProcessor
import boringyuri.processor.common.steps.IndependentUriDataGeneratorStep
import boringyuri.processor.common.steps.type.CommonTypeName.OVERRIDE
import boringyuri.processor.common.steps.util.AnnotationHandler
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName

class IndependentUriDataProcessor(environment: SymbolProcessorEnvironment) :
    KspBoringAnnotationProcessor(environment) {
    override fun initSteps(session: ProcessingSession): Iterable<BoringProcessingStep> {
        val annotationHandler = AnnotationHandler(INTERNAL_ANNOTATIONS)

        return setOf(
            IndependentUriDataGeneratorStep(session, annotationHandler)
        )
    }

    companion object {
        private val INTERNAL_ANNOTATIONS: Set<TypeName> = hashSetOf(
            OVERRIDE,
            ClassName.get(UriData::class.java),
            ClassName.get(Path::class.java),
            ClassName.get(Param::class.java),
            ClassName.get(DefaultValue::class.java),
            ClassName.get(TypeAdapter::class.java)
        )
    }
}
