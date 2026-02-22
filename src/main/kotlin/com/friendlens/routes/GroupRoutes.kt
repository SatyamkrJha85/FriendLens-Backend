package com.friendlens.routes

import com.friendlens.models.Groups
import com.friendlens.models.GroupMembers
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import java.util.UUID
import java.time.LocalDateTime
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString

fun Route.groupRoutes() {
    route("/api/groups") {
        
        // Ensure user is signed in to create or view groups
        authenticate("auth-jwt") {
            
            // Generate a random 6 character join code
            fun generateJoinCode(): String {
                val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
                return (1..6).map { chars.random() }.joinToString("")
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()
                val params = call.receive<Map<String, String>>()
                
                val name = params["name"]
                val description = params["description"]
                
                if (userIdStr == null || name == null) {
                    call.respond(mapOf("status" to "error", "message" to "Missing user ID or group name"))
                    return@post
                }
                
                val userId = UUID.fromString(userIdStr)
                val newGroupId = UUID.randomUUID()
                val code = generateJoinCode()

                transaction {
                    // Create the Group
                    Groups.insert {
                        it[id] = newGroupId
                        it[Groups.name] = name
                        it[Groups.description] = description
                        it[ownerId] = userId
                        it[joinCode] = code
                        it[createdAt] = LocalDateTime.now()
                    }

                    // Add the creator as the "owner" member
                    GroupMembers.insert {
                        it[groupId] = newGroupId
                        it[GroupMembers.userId] = userId
                        it[role] = "owner"
                        it[joinedAt] = LocalDateTime.now()
                    }
                }

                call.respond(buildJsonObject {
                    put("status", "success")
                    put("group", buildJsonObject {
                        put("id", newGroupId.toString())
                        put("name", name)
                        put("description", description ?: "")
                        put("joinCode", code)
                    })
                })
            }

            post("/join") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()
                val params = call.receive<Map<String, String>>()
                val joinCode = params["joinCode"]?.uppercase()

                if (userIdStr == null || joinCode == null) {
                    call.respond(mapOf("status" to "error", "message" to "Missing user ID or join code"))
                    return@post
                }

                val userId = UUID.fromString(userIdStr)

                try {
                    val groupData = transaction {
                        val groupRow = Groups.selectAll().where { Groups.joinCode eq joinCode }.singleOrNull()
                        
                        if (groupRow != null) {
                            val groupId = groupRow[Groups.id]
                            
                            // Check if they are already in the group
                            val existingMember = GroupMembers.selectAll().where { 
                                (GroupMembers.groupId eq groupId) and (GroupMembers.userId eq userId)
                            }.singleOrNull()

                            if (existingMember == null) {
                                GroupMembers.insert {
                                    it[GroupMembers.groupId] = groupId
                                    it[GroupMembers.userId] = userId
                                    it[role] = "member"
                                    it[joinedAt] = LocalDateTime.now()
                                }
                            }
                            
                            buildJsonObject {
                                put("id", groupId.toString())
                                put("name", groupRow[Groups.name])
                            }
                        } else {
                            null
                        }
                    }

                    if (groupData != null) {
                        call.respond(buildJsonObject {
                            put("status", "success")
                            put("message", "Joined group!")
                            put("group", groupData)
                        })
                    } else {
                        call.respond(mapOf("status" to "error", "message" to "Invalid join code"))
                    }

                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to e.localizedMessage))
                }
            }

            get {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()
                
                if (userIdStr == null) {
                    call.respond(mapOf("status" to "error", "message" to "Missing user ID"))
                    return@get
                }
                
                val userId = UUID.fromString(userIdStr)

                try {
                    val groups = transaction {
                        // Find all group ids this user is a member of
                        val groupIds = GroupMembers.selectAll().where { GroupMembers.userId eq userId }
                            .map { it[GroupMembers.groupId] }

                        if (groupIds.isEmpty()) return@transaction buildJsonArray {}

                        // Fetch those groups
                        buildJsonArray {
                            Groups.selectAll().where { Groups.id inList groupIds }.forEach {
                                add(buildJsonObject {
                                    put("id", it[Groups.id].toString())
                                    put("name", it[Groups.name])
                                    put("description", it[Groups.description] ?: "")
                                    put("joinCode", it[Groups.joinCode])
                                })
                            }
                        }
                    }

                    call.respond(buildJsonObject {
                        put("status", "success")
                        put("groups", groups)
                    })
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to e.localizedMessage))
                }
            }

            get("/{id}") {
                val groupIdStr = call.parameters["id"]
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()

                if (groupIdStr == null || userIdStr == null) {
                    call.respond(mapOf("status" to "error", "message" to "Invalid request"))
                    return@get
                }

                val userId: UUID
                val groupId: UUID
                try {
                    userId = UUID.fromString(userIdStr)
                    groupId = UUID.fromString(groupIdStr)
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to "Invalid group ID format"))
                    return@get
                }

                try {
                    val groupDetail = transaction {
                        // Validate membership
                        val isMember = GroupMembers.selectAll().where { 
                            (GroupMembers.groupId eq groupId) and (GroupMembers.userId eq userId) 
                        }.count() > 0

                        if (!isMember) return@transaction null

                        // Fetch group
                        val groupRow = Groups.selectAll().where { Groups.id eq groupId }.singleOrNull()
                        
                        if (groupRow != null) {
                            buildJsonObject {
                                put("id", groupRow[Groups.id].toString())
                                put("name", groupRow[Groups.name])
                                put("description", groupRow[Groups.description] ?: "")
                                put("joinCode", groupRow[Groups.joinCode])
                                put("createdAt", groupRow[Groups.createdAt].toString())
                            }
                        } else null
                    }

                    if (groupDetail != null) {
                        call.respond(buildJsonObject {
                            put("status", "success")
                            put("group", groupDetail)
                        })
                    } else {
                        call.respond(mapOf("status" to "error", "message" to "Group not found or access denied"))
                    }
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to e.localizedMessage))
                }
            }
        }
    }
}
