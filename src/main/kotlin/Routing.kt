package com.friendlens

import com.friendlens.routes.testRoutes
import com.friendlens.routes.userRoutes
import com.friendlens.routes.groupRoutes
import com.friendlens.routes.photoRoutes
import com.friendlens.routes.feedbackRoutes
import com.asyncapi.kotlinasyncapi.context.service.AsyncApiExtension
import com.asyncapi.kotlinasyncapi.ktor.AsyncApiPlugin
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleCache.cacheOutput
import com.ucasoft.ktor.simpleMemoryCache.*
import com.ucasoft.ktor.simpleRedisCache.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("FriendLens API MVP is running!")
        }
        
        // Mount API endpoints
        testRoutes()
        userRoutes()
        groupRoutes()
        photoRoutes()
        feedbackRoutes()
    }
}
