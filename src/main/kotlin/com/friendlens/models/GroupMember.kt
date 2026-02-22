package com.friendlens.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object GroupMembers : Table("group_members") {
    val groupId = uuid("group_id").references(Groups.id)
    val userId = uuid("user_id").references(Users.id)
    val role = varchar("role", 20).default("member") // "owner", "admin", "member"
    val joinedAt = datetime("joined_at")

    override val primaryKey = PrimaryKey(groupId, userId)
}
