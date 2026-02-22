package com.friendlens.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Users : Table("users") {
    // We map this strictly to the Supabase Auth UUID
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 255).nullable()
    val avatarUrl = text("avatar_url").nullable()
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
