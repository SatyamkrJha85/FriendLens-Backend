package com.friendlens.routes

import com.friendlens.models.Feedbacks
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.time.LocalDateTime
import kotlinx.serialization.json.*

fun Route.feedbackRoutes() {
    route("/api/feedback") {
        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()
                val params = call.receive<Map<String, String>>()
                
                val content = params["content"]
                val ratingStr = params["rating"]
                
                if (userIdStr == null || content == null) {
                    call.respond(mapOf("status" to "error", "message" to "Missing user ID or content"))
                    return@post
                }
                
                val userId: UUID
                try {
                    userId = UUID.fromString(userIdStr)
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to "Invalid user ID format"))
                    return@post
                }
                
                val rating = ratingStr?.toIntOrNull()
                val feedbackId = UUID.randomUUID()
                
                transaction {
                    Feedbacks.insert {
                        it[id] = feedbackId
                        it[Feedbacks.userId] = userId
                        it[Feedbacks.content] = content
                        it[Feedbacks.rating] = rating
                        it[createdAt] = LocalDateTime.now()
                    }
                }
                
                call.respond(buildJsonObject {
                    put("status", "success")
                    put("message", "Feedback submitted successfully. Thank you!")
                })
            }
        }
    }
}
