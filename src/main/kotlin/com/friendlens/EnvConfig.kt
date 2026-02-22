package com.friendlens

import io.github.cdimascio.dotenv.dotenv

/**
 * Singleton to easily access environmental variables loaded dynamically via Dotenv.
 */
object EnvConfig {
    private val env = dotenv {
        ignoreIfMissing = true
    }

    operator fun get(key: String): String? = env[key] ?: System.getenv(key)

    fun getOrThrow(key: String): String = get(key) ?: throw IllegalStateException("Missing environment variable: $key")
}
