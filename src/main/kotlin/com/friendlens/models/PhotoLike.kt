package com.friendlens.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object PhotoLikes : Table("photo_likes") {
    val photoId = uuid("photo_id").references(Photos.id)
    val userId = uuid("user_id").references(Users.id)
    val likedAt = datetime("liked_at")

    override val primaryKey = PrimaryKey(photoId, userId)
}
