/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.instantexecution.serialization.codecs.transform

import org.gradle.api.internal.DomainObjectContext
import org.gradle.api.internal.artifacts.transform.TransformationStep
import org.gradle.api.internal.artifacts.transform.Transformer
import org.gradle.api.internal.artifacts.transform.TransformerInvocationFactory
import org.gradle.api.internal.project.ProjectStateRegistry
import org.gradle.instantexecution.serialization.Codec
import org.gradle.instantexecution.serialization.ReadContext
import org.gradle.instantexecution.serialization.WriteContext
import org.gradle.instantexecution.serialization.readNonNull
import org.gradle.internal.fingerprint.FileCollectionFingerprinterRegistry


internal
class TransformationStepCodec(
    private val projectStateRegistry: ProjectStateRegistry,
    private val fingerprinterRegistry: FileCollectionFingerprinterRegistry
) : Codec<TransformationStep> {

    override suspend fun WriteContext.encode(value: TransformationStep) {
        val project = value.owningProject ?: throw UnsupportedOperationException("Transformation must have an owning project to be encoded.")
        writeString(project.path)
        write(value.transformer)
    }

    override suspend fun ReadContext.decode(): TransformationStep {
        val path = readString()
        val transformer = readNonNull<Transformer>()
        val project = getProject(path)
        val services = project.services
        return TransformationStep(
            transformer,
            services[TransformerInvocationFactory::class.java],
            services[DomainObjectContext::class.java],
            projectStateRegistry,
            fingerprinterRegistry
        )
    }
}
