package com.friendlens.routes

import com.friendlens.plugins.StorageService
import com.friendlens.plugins.TestTable
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request

fun Route.testRoutes() {
    route("/api/test") {
        get("/db") {
            try {
                // Return total count of TestTable to ensure DB connection is active and responsive.
                val count = transaction {
                    TestTable.selectAll().count()
                }
                call.respond(mapOf("status" to "success", "message" to "DB connected successfully.", "testTableCount" to count.toString()))
            } catch (e: Exception) {
                call.respond(mapOf("status" to "error", "message" to e.message))
            }
        }

        get("/s3") {
            try {
                // List files in the root of the Imgbucket
                val s3 = StorageService.s3
                val bucket = StorageService.bucketName
                val req = ListObjectsV2Request.builder().bucket(bucket).maxKeys(10).build()
                val res = s3.listObjectsV2(req)
                
                val objects = res.contents().map { it.key() }
                
                call.respond(mapOf(
                    "status" to "success", 
                    "message" to "S3 connected successfully. Checked bucket: $bucket",
                    "files" to objects
                ))
            } catch (e: Exception) {
                call.respond(mapOf("status" to "error", "message" to e.message))
            }
        }
    }
}
