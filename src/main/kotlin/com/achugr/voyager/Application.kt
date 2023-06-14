package com.achugr.voyager

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.statuspages.*
import space.jetbrains.api.runtime.ktorClientForSpace

@Suppress("unused")
fun Application.module() {

    configureRouting()
}

fun main() {
    embeddedServer(CIO, port = config.getInt("http.port")) {
        module()
        install(StatusPages)
    }.start(wait = true)
}

val spaceHttpClient = ktorClientForSpace()

val config: Config by lazy { ConfigFactory.load() }