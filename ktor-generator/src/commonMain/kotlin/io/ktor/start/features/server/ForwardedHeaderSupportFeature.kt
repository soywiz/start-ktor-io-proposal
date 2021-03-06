/*
 * Copyright 2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.ktor.start.features.server

import io.ktor.start.*
import io.ktor.start.project.*
import io.ktor.start.util.*

object ForwardedHeaderSupportFeature : ServerFeature(ApplicationKt) {
    override val artifacts = listOf("io.ktor:ktor-server-core:\$ktor_version")
    override val id = "forwarded-header-support"
    override val title = "ForwardedHeaderSupport"
    override val description = "This feature allows you to handle reverse proxy headers to get information about the original request when it’s behind a proxy."
    override val documentation = "https://ktor.io/docs/forward-headers.html"

    override fun BlockBuilder.renderFeature(info: BuildInfo) {
        addImport("io.ktor.features.*")
        addFeatureInstall {
            +"install(ForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy"
            +"install(XForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy"
        }
    }
}
