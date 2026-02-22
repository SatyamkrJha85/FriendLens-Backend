package com.friendlens

import com.auth0.jwt.JWT
import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import java.net.URL
import java.util.concurrent.TimeUnit
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    val supabaseUrl = EnvConfig.getOrThrow("SUPABASE_URL")
    
    val jwksUrl = URL("$supabaseUrl/auth/v1/.well-known/jwks.json")
    val jwkProvider = JwkProviderBuilder(jwksUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "FriendLens Backend"
            verifier(jwkProvider, "$supabaseUrl/auth/v1") {
                acceptLeeway(5)
            }
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
