package com.friendlens.routes

import com.friendlens.models.GroupMembers
import com.friendlens.models.Groups
import com.friendlens.models.Photos
import com.friendlens.models.Users
import com.friendlens.plugins.StorageService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.content.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.serialization.json.*

fun Route.photoRoutes() {
    route("/api/groups/{id}/photos") {
        
        authenticate("auth-jwt") {
            
            get {
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
                    val photos = transaction {
                        // Validate membership
                        val isMember = GroupMembers.selectAll().where { 
                            (GroupMembers.groupId eq groupId) and (GroupMembers.userId eq userId) 
                        }.count() > 0

                        if (!isMember) return@transaction null
                        // Fetch users to map userIds to their profiles
                        val userIds = Photos.selectAll().where { Photos.groupId eq groupId }.map { it[Photos.uploadedBy] }.distinct()
                        val usersMap = Users.selectAll().where { Users.id inList userIds }.associateBy({ it[Users.id] }, { row ->
                            mapOf(
                                "username" to (row[Users.username] ?: "Unknown User"),
                                "avatarUrl" to (row[Users.avatarUrl] ?: "")
                            )
                        })

                        buildJsonArray {
                            Photos.selectAll().where { Photos.groupId eq groupId }.forEach { row ->
                                val uId = row[Photos.uploadedBy]
                                add(buildJsonObject {
                                    put("id", row[Photos.id].toString())
                                    put("s3KeyOriginal", row[Photos.s3KeyOriginal] ?: "")
                                    put("s3KeyThumbnail", row[Photos.s3KeyThumbnail] ?: "")
                                    put("uploadedBy", uId.toString())
                                    put("uploadedByUsername", usersMap[uId]?.get("username") ?: "Unknown User")
                                    put("uploadedByAvatar", usersMap[uId]?.get("avatarUrl") ?: "")
                                    put("uploadedAt", row[Photos.uploadedAt].toString())
                                    put("capturedAt", row[Photos.capturedAt]?.toString() ?: "")
                                    put("fileSizeBytes", row[Photos.fileSizeBytes]?.toString() ?: "")
                                })
                            }
                        }
                    }

                    if (photos != null) {
                        call.respond(buildJsonObject {
                            put("status", "success")
                            put("photos", photos)
                        })
                    } else {
                        call.respond(mapOf("status" to "error", "message" to "Access denied or group not found"))
                    }
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to e.localizedMessage))
                }
            }

            post("/upload") {
                val groupIdStr = call.parameters["id"]
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("sub")?.asString()

                if (groupIdStr == null || userIdStr == null) {
                    call.respond(mapOf("status" to "error", "message" to "Invalid request"))
                    return@post
                }

                val userId: UUID
                val groupId: UUID
                try {
                    userId = UUID.fromString(userIdStr)
                    groupId = UUID.fromString(groupIdStr)
                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to "Invalid group ID format"))
                    return@post
                }

                // 1. Check if user belongs to this group
                val isMember = transaction {
                    GroupMembers.selectAll().where { 
                        (GroupMembers.groupId eq groupId) and (GroupMembers.userId eq userId) 
                    }.count() > 0
                }

                if (!isMember) {
                    call.respond(mapOf("status" to "error", "message" to "You do not belong to this group"))
                    return@post
                }

                // 2. Read Multipart Data (Bypassing pre-signed for simpler MVP flow as requested)
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var originalFileName: String? = null
                var capturedAtRaw: String? = null

                multipart.forEachPart { part ->
                    if (part is io.ktor.http.content.PartData.FileItem) {
                        originalFileName = part.originalFileName ?: "image.jpg"
                        fileBytes = part.streamProvider().readBytes()
                    } else if (part is io.ktor.http.content.PartData.FormItem) {
                        if (part.name == "capturedAt") {
                            capturedAtRaw = part.value
                        }
                    }
                    part.dispose()
                }

                if (fileBytes == null) {
                    call.respond(mapOf("status" to "error", "message" to "No file found in upload"))
                    return@post
                }

                // 3. Upload to S3
                val fileExtension = originalFileName?.substringAfterLast('.', "jpg") ?: "jpg"
                val s3Key = "groups/$groupId/photos/${UUID.randomUUID()}.$fileExtension"
                
                try {
                    val putReq = PutObjectRequest.builder()
                        .bucket(StorageService.bucketName)
                        .key(s3Key)
                        .contentType("image/jpeg") 
                        .build()

                    StorageService.s3.putObject(putReq, RequestBody.fromBytes(fileBytes!!))
                    
                    // 4. Record to Database
                    val capturedAtTime = capturedAtRaw?.let { 
                        try { LocalDateTime.parse(it) } catch (e: Exception) { null }
                    }

                    val newPhotoId = UUID.randomUUID()

                    transaction {
                        Photos.insert {
                            it[id] = newPhotoId
                            it[Photos.groupId] = groupId
                            it[uploadedBy] = userId
                            it[s3KeyOriginal] = s3Key
                            it[s3KeyThumbnail] = null // Will be handled asynchronously in future
                            it[capturedAt] = capturedAtTime
                            it[uploadedAt] = LocalDateTime.now()
                            it[fileSizeBytes] = fileBytes?.size?.toLong() ?: 0L
                        }
                    }

                    call.respond(buildJsonObject {
                        put("status", "success")
                        put("photo", buildJsonObject {
                            put("id", newPhotoId.toString())
                            put("originalUrl", "${StorageService.s3.utilities().getUrl { it.bucket(StorageService.bucketName).key(s3Key) }}")
                            put("s3Key", s3Key)
                        })
                    })

                } catch (e: Exception) {
                    call.respond(mapOf("status" to "error", "message" to "Upload to S3 failed: ${e.message}"))
                }
            }
        }
    }
}
