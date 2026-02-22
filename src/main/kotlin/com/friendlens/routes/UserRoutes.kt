package com.friendlens.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import io.ktor.server.request.*
import kotlinx.serialization.json.*

fun Route.userRoutes() {
    route("/api/users") {
        
        // This endpoint requires a valid Supabase JWT passed in the Authorization header
        // e.g. "Authorization: Bearer <token>"
        authenticate("auth-jwt") {
            
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("sub")?.asString()
                val email = principal?.payload?.getClaim("email")?.asString()
                
                if (userId != null) {
                    val uuid = java.util.UUID.fromString(userId)
                    val emailStr = email ?: ""
                    
                    org.jetbrains.exposed.sql.transactions.transaction {
                        val exists = com.friendlens.models.Users.selectAll()
                            .where { com.friendlens.models.Users.id eq uuid }.count() > 0
                            
                        if (!exists) {
                            com.friendlens.models.Users.insert {
                                it[id] = uuid
                                it[com.friendlens.models.Users.email] = emailStr
                                it[createdAt] = java.time.LocalDateTime.now()
                            }
                        }
                    }

                    // Fetch the latest user info
                    val userInfo = org.jetbrains.exposed.sql.transactions.transaction {
                        com.friendlens.models.Users.selectAll()
                            .where { com.friendlens.models.Users.id eq uuid }
                            .singleOrNull()
                    }
                    
                    val currentUsername = userInfo?.get(com.friendlens.models.Users.username)
                    val currentAvatar = userInfo?.get(com.friendlens.models.Users.avatarUrl)

                    call.respond(buildJsonObject {
                        put("status", "success")
                        put("userId", userId)
                        put("email", emailStr)
                        put("username", currentUsername ?: "")
                        put("avatarUrl", currentAvatar ?: "")
                    })
                } else {
                    call.respond(buildJsonObject {
                        put("status", "error")
                        put("message", "Could not extract user from token")
                    })
                }
            }
            
            put("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()
                
                if (userIdStr == null) {
                    call.respond(buildJsonObject {
                        put("status", "error")
                        put("message", "Could not extract user from token")
                    })
                    return@put
                }
                
                val params = call.receive<Map<String, String>>()
                val newUsername = params["username"]
                val newAvatarUrl = params["avatarUrl"]
                
                val uuid = java.util.UUID.fromString(userIdStr)
                
                org.jetbrains.exposed.sql.transactions.transaction {
                    com.friendlens.models.Users.update({ com.friendlens.models.Users.id eq uuid }) {
                        if (newUsername != null) it[username] = newUsername
                        if (newAvatarUrl != null) it[avatarUrl] = newAvatarUrl
                    }
                }
                
                call.respond(buildJsonObject {
                    put("status", "success")
                    put("message", "Profile updated successfully")
                })
            }
        }
    }
}
