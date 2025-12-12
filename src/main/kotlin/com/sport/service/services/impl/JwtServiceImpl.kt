package com.sport.service.services.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sport.service.entities.Subscriber
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtServiceImpl(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long
) {

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(subscriber: Subscriber): String {
        val now = System.currentTimeMillis()
        val expires = Date(now + expirationMs)

        return JWT.create()
            .withSubject(subscriber.id.toString())
            .withClaim("username", subscriber.username)
            .withClaim("id", subscriber.id)
            .withClaim("role", subscriber.role.name)
            .withIssuedAt(Date(now))
            .withExpiresAt(expires)
            .sign(algorithm)
    }
}