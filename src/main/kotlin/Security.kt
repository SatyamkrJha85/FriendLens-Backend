package com.friendlens

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val jwtSecret = EnvConfig.getOrThrow("SUPABASE_JWT_SECRET")

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "FriendLens Backend"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    // Supabase issues tokens without explicitly matching an audience usually for simple access, 
                    // or you can configure `withAudience("authenticated")`. Let's keep it relaxed or check "aud" manually.
                    .build()
            )
            validate { credential ->
                // The "role" claim usually distinguishes auth vs anon in Supabase
                val role = credential.payload.getClaim("role").asString()
                val sub = credential.payload.getClaim("sub").asString()
                val audList = credential.payload.audience
                val audStr = credential.payload.getClaim("aud").asString()
                val isAuthedAudience = audList?.contains("authenticated") == true || audStr == "authenticated" || audStr == null

                if (sub != null && role == "authenticated" && isAuthedAudience) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("status" to "error", "message" to "Token is missing, invalid, or expired"))
            }
        }
    }
}
