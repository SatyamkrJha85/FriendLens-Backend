package com.friendlens.plugins

import com.friendlens.EnvConfig
import com.friendlens.models.Groups
import com.friendlens.models.GroupMembers
import com.friendlens.models.Users
import com.friendlens.models.Photos
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Table

/**
 * Example simple test table to verify Postgres connection logic.
 */
object TestTable : Table("test_table") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    override val primaryKey = PrimaryKey(id)
}

object DatabaseSingleton {
    lateinit var database: Database
}

fun Application.configureDatabases() {
    val jdbcUrl = EnvConfig.getOrThrow("JDBC_DATABASE_URL")
    val user = EnvConfig.getOrThrow("DB_USER")
    val password = EnvConfig.getOrThrow("DB_PASSWORD")

    val hikariConfig = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        this.jdbcUrl = jdbcUrl
        this.username = user
        this.password = password
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    try {
        val dataSource = HikariDataSource(hikariConfig)
        DatabaseSingleton.database = Database.connect(dataSource)

        transaction {
            SchemaUtils.create(TestTable, Users, Groups, GroupMembers, Photos)
        }
        log.info(" Postgres Database connected successfully!")
    } catch (e: Exception) {
        log.error(" Postgres Database connection failed! Error: ${e.message}")
    }
}
