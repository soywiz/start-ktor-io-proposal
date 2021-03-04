package io.ktor.start.features.client

import io.ktor.start.*
import io.ktor.start.project.*
import io.ktor.start.util.*


abstract class ClientEngine(vararg deps: Block<BuildInfo>) : ClientFeature(*deps) {
    override val group = "HttpClient Engine"
}

object CoreClientEngine : ClientEngine(ApplicationKt) {
    override val id = "ktor-client-core"
    override val title = "HttpClient Engine"
    override val description = "Core of the HttpClient. Required for libraries."
    override val documentation = "https://ktor.io/docs/clients-index.html"

    override val artifacts: List<String> by lazy {
        listOf(
            "io.ktor:ktor-client-core:\$ktor_version",
            "io.ktor:ktor-client-core-jvm:\$ktor_version"
        )
    }

    val CLIENT_USAGE = newSlot("CLIENT")
    val CLIENT_FEATURES = newSlot("CLIENT_FEATURES")

    override fun BlockBuilder.renderFeature(info: BuildInfo) {
        addImport("io.ktor.client.*")

        appendSeparated(ApplicationKt.MODULE_INSTALL) {
            linedeferred {
                "val client = HttpClient($clientEngine)" {
                    block(CLIENT_FEATURES)
                }
            }
            block(CLIENT_USAGE)
        }
    }
}

internal var BlockBuilder.clientEngine: String by Extra.PropertyThis { "" }


object ApacheClientEngine : ClientEngine(CoreClientEngine) {
    override val id: String = "ktor-client-apache"
    override val title = "Apache HttpClient Engine"
    override val description = "Engine for the Ktor HttpClient using Apache. Supports HTTP 1.x and HTTP 2.0."
    override val documentation = "https://ktor.io/docs/http-client-engines.html#apache"

    override fun BlockBuilder.renderFeature(info: BuildInfo) {
        addImport("io.ktor.client.engine.apache.*")
        clientEngine = "Apache"
    }
}

object CioClientEngine : ClientEngine(CoreClientEngine) {
    override val id: String = "ktor-client-cio"
    override val title = "CIO HttpClient Engine"
    override val description = "Engine for the Ktor HttpClient using CIO (Corroutine I/O). Only supports HTTP 1.x."
    override val documentation = "https://ktor.io/docs/http-client-engines.html#cio"

    override val artifacts: List<String> by lazy {
        listOf(
            "io.ktor:ktor-client-cio:\$ktor_version",
            "io.ktor:ktor-client-cio-jvm:\$ktor_version"
        )
    }
    
    override fun BlockBuilder.renderFeature(info: BuildInfo) {
        addImport("io.ktor.client.engine.cio.*")
        clientEngine = "CIO"
    }
}

object JettyClientEngine : ClientEngine(CoreClientEngine) {
    override val id: String = "ktor-client-jetty"
    override val title = "Jetty HttpClient Engine"
    override val description = "Engine for the Ktor HttpClient using Jetty. Only supports HTTP 2.x."
    override val documentation = "https://ktor.io/docs/http-client-engines.html#jetty"

    override fun BlockBuilder.renderFeature(info: BuildInfo) {
        addImport("io.ktor.client.engine.jetty.*")
        clientEngine = "Jetty"
    }
}

object MockClientEngine : ClientEngine(CoreClientEngine, ApplicationTestKt) {
    override val id: String = "ktor-client-mock"
    override val title = "Mock HttpClient Engine"
    override val description = "Engine for using in tests to simulate HTTP responses programmatically."
    override val documentation = "https://ktor.io/docs/http-client-engines.html#mockengine"

    override val artifacts = listOf<String>()
    override val testArtifacts = listOf(
        "io.ktor:ktor-client-mock:\$ktor_version",
        "io.ktor:ktor-client-mock-jvm:\$ktor_version"
    )

    override fun BlockBuilder.renderFeature(info: BuildInfo) {
        addTestImport("io.ktor.client.*")
        addTestImport("io.ktor.client.engine.mock.*")
        addTestImport("io.ktor.client.request.*")
        addTestImport("io.ktor.client.statement.*")
        addTestImport("io.ktor.http.*")
        addTestImport("io.ktor.utils.io.*")
        addTestImport("kotlinx.coroutines.*")
        addTestImport("kotlin.test.*")

        addTestMethod("testClientMock") {
            +"runBlocking" {
                when {
                    info.ktorVersion >= Versions.V120 -> {
                        +"val client = HttpClient(MockEngine) {"
                        indent {
                            +"engine" {
                                +"addHandler { request -> "
                                indent {
                                    +"when (request.url.fullPath)" {
                                        +"\"/\" -> respond("
                                        indent {
                                            +"ByteReadChannel(byteArrayOf(1, 2, 3)),"
                                            +"headers = headersOf(\"X-MyHeader\", \"MyValue\")"
                                        }
                                        +")"
                                        +"else -> respond(\"Not Found \${request.url.encodedPath}\", HttpStatusCode.NotFound)"
                                    }
                                }
                                +"}"
                            }
                            +"expectSuccess = false"
                        }
                        +"}"
                    }
                    info.ktorVersion >= Versions.V100 -> {
                        +"val client = HttpClient(MockEngine {"
                        indent {
                            +"if (url.encodedPath == \"/\")" {
                                +"MockHttpResponse(call, HttpStatusCode.OK, ByteReadChannel(byteArrayOf(1, 2, 3)), headersOf(\"X-MyHeader\", \"MyValue\"))"
                            }
                            appending("else") {
                                +"responseError(HttpStatusCode.NotFound, \"Not Found \${url.encodedPath}\")"
                            }
                        }
                        +"})" {
                            +"expectSuccess = false"
                        }
                    }
                    else -> {
                        +"val client = HttpClient(MockEngine { call ->"
                        indent {
                            +"if (url.encodedPath == \"/\")" {
                                +"MockHttpResponse(call, HttpStatusCode.OK, ByteReadChannel(byteArrayOf(1, 2, 3)), headersOf(\"X-MyHeader\", \"MyValue\"))"
                            }
                            appending("else") {
                                +"MockHttpResponse(call, HttpStatusCode.NotFound, ByteReadChannel(\"Not Found \${url.encodedPath}\"))"
                            }
                        }
                        +"})"
                    }
                }

                +"assertEquals(byteArrayOf(1, 2, 3).toList(), client.get<ByteArray>(\"/\").toList())"
                +"assertEquals(\"MyValue\", client.request<HttpResponse>(\"/\").headers[\"X-MyHeader\"])"
                +"assertEquals(\"Not Found other/path\", client.get<String>(\"/other/path\"))"
            }
        }
    }
}
