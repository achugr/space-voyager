package com.achugr.voyager

import AppHasPermissionsService
import Routes
import com.achugr.voyager.service.GetGraphService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jvmMain.space.jetbrains.api.runtime.helpers.appInstanceStorage
import kotlinx.serialization.Serializable
import space.jetbrains.api.runtime.Space
import space.jetbrains.api.runtime.helpers.RequestAdapter
import space.jetbrains.api.runtime.helpers.SpaceHttpResponse
import space.jetbrains.api.runtime.helpers.processPayload
import space.jetbrains.api.runtime.types.InitPayload

fun Application.configureRouting() {
    install(Locations)
    install(ContentNegotiation) {
        json()
    }

    routing {
        post("/api/space") {
            Space.processPayload(ktorRequestAdapter(call), spaceHttpClient, appInstanceStorage) { payload ->
                when (payload) {
                    is InitPayload -> {
                        setUiExtensions()
                        SpaceHttpResponse.RespondWithOk
                    }

                    else -> {
                        call.respond(HttpStatusCode.OK)
                        SpaceHttpResponse.RespondWithOk
                    }
                }
            }
        }
        static("/space-iframe") {
            staticBasePackage = "space-iframe"
            resources(".")
            defaultResource("index.html")
        }
        static("/") {
            staticBasePackage = "space-iframe"
            resources(".")
            defaultResource("index.html")
        }

        get("/health") {
            call.respond(HttpStatusCode.OK)
        }

        get("/api/info") {
            call.respond(HttpStatusCode.OK, AppInfo(config.getString("mode") == "local"))
        }

        get<Routes.AppHasPermissions> {
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, AppHasPermissionsService(spaceTokenInfo).appHasPermissions())
            }
        }

        get<Routes.GetGraph> { getGraph ->
            runAuthorized { spaceTokenInfo ->
                call.respond(HttpStatusCode.OK, GetGraphService(spaceTokenInfo).getGraph())
            }
        }

        post<Routes.GrabGraph> { params ->
            runAuthorized { spaceTokenInfo ->
                Grabber(spaceTokenInfo.appSpaceClient()).grab()
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

@Serializable
data class AppInfo(private val isLocal: Boolean) {
}

private fun ktorRequestAdapter(call: ApplicationCall): RequestAdapter {
    return object : RequestAdapter {
        override suspend fun receiveText() =
            call.receiveText()

        override fun getHeader(headerName: String) =
            call.request.header(headerName)

        override suspend fun respond(httpStatusCode: Int, body: String) =
            call.respond(HttpStatusCode.fromValue(httpStatusCode), body)
    }
}
