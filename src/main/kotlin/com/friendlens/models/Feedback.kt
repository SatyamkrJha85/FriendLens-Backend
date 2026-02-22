package com.friendlens.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object Feedbacks : Table("feedbacks") {
    val id = uuid("id")
    val userId = uuid("user_id").references(Users.id)
    val content = text("content")
    val rating = integer("rating").nullable() // optional 1-5 rating
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
