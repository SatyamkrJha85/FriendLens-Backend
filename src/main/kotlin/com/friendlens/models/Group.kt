package com.friendlens.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Groups : Table("groups") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val coverImageUrl = text("cover_image_url").nullable()
    val ownerId = uuid("owner_id").references(Users.id)
    val joinCode = varchar("join_code", 10).uniqueIndex() // Ex: "AX7B9Z"
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
