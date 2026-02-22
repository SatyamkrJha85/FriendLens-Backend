package com.friendlens

import com.friendlens.plugins.configureDatabases
import com.friendlens.plugins.configureSerialization
import com.friendlens.plugins.configureStorage
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureDatabases()
    configureStorage()
    configureRouting()
}
