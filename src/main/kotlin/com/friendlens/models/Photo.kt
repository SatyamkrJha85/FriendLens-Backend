package com.friendlens.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Photos : Table("photos") {
    val id = uuid("id").autoGenerate()
    val groupId = uuid("group_id").references(Groups.id)
    val uploadedBy = uuid("uploaded_by").references(Users.id)
    val s3KeyOriginal = text("s3_key_original")
    val s3KeyThumbnail = text("s3_key_thumbnail").nullable()
    val capturedAt = datetime("captured_at").nullable()
    val uploadedAt = datetime("uploaded_at")
    val fileSizeBytes = long("file_size_bytes").nullable()

    override val primaryKey = PrimaryKey(id)
}
