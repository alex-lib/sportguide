package com.sport.service.services.impl

import JwtServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.sport.service.dto.SubscriberDto
import com.sport.service.entities.Subscriber
import com.sport.service.services.SubscriberService
import com.sport.service.web.models.auth.JwtResponse
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.meta.api.objects.User
import java.net.URLDecoder
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TelegramAuthServiceImpl(
    private val subscriberService: SubscriberService,
    private val jwtServiceImpl: JwtServiceImpl,
    private val objectMapper: ObjectMapper,
    @Value("\${telegram.bot-token}") private val botToken: String
) {

    private val secretKey: ByteArray = MessageDigest.getInstance("SHA-256").digest(botToken.toByteArray())

    //https://github.com/riobits/Telegram-Web-API-Cheatsheet?utm_source=chatgpt.com
    fun authenticate(initData: String): JwtResponse {
        val data: Map<String, String> = parseInitData(initData)

        if (!validateTelegramHash(data)) throw RuntimeException("Invalid Telegram auth data")
        val userJson: String = data["user"] ?: throw RuntimeException("Invalid Telegram auth data")

        val tgUser = objectMapper.readValue(userJson, SubscriberDto::class.java)
        val subscriber: Subscriber = subscriberService.findById(tgUser.id)

        if (subscriber != null) {
            subscriber.username = tgUser.username
            subscriber.firstName = tgUser.firstName
            subscriber.lastName = tgUser.lastName
            subscriberService.updateSubscriber(subscriber, tgUser.id)
        } else {
            val user: User = User()
            user.userName = tgUser.username
            user.id = tgUser.id
            user.lastName = tgUser.lastName
            user.firstName = tgUser.firstName
            subscriberService.addSubscriber(user)
        }
        return JwtResponse(jwtServiceImpl.generateToken(subscriberService.findById(tgUser.id)))
    }

    //example:
    //query_id=AAGXJt8AAAAAAafJt3xYdPq3 - id сессии webapp (может отсутствовать)
    //&user=%7B%22id%22%3A123456789%2C%22username%22%3A%22alex_sport%22%2C%22first_name%22%3A%22Алекс%22%2C%22last_name%22%3A%22Иванов%22%2C%22language_code%22%3A%22ru%22%7D - json, но encoded
    //&auth_date=123 - timestamp, когда telegram сгенерировал данные
    //&hash=9e4f2b4c6f0c3d8e7b3b1e6c1a2d4f8a9c7e4b2ff3c9d1a6b4f2c8c9d1e0f2a - подпись, для проверки подлинности данных
    private fun parseInitData(initData: String): MutableMap<String, String> {
        val result: MutableMap<String, String> = mutableMapOf()

        val pairs: List<String> = initData.split("&")
        for (pair in pairs) {
            val keyAndValue: List<String> = pair.split("=", limit = 2)
            val key: String = keyAndValue[0]
            val value: String = keyAndValue[1]
            val decodedValue = URLDecoder.decode(value, "UTF-8")
            result[key] = decodedValue
        }
        return result
    }

    //https://core.telegram.org/widgets/login?utm_source=chatgpt.com
    private fun validateTelegramHash(data: Map<String, String>): Boolean {
        val receivedHash: String = data["hash"] ?: return false

        val filtered: Map<String, String> = data.filterKeys { it != "hash" }
        val sorted: Map<String, String> = filtered.toSortedMap()
        val parts: List<String> = sorted.map { entry -> "${entry.key}=${entry.value}" }
        val dataCheckString: String = parts.joinToString("\n")
        //example:
        //auth_date=123
        //user={"id":123}
        //query_id=AAGXJt8AAAAAAafJt3xYdPq3

        val mac: Mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        val calculated: ByteArray = mac.doFinal(dataCheckString.toByteArray())

        val calculatedHash: String = calculated.joinToString("") { "%02x".format(it) }
        return calculatedHash == receivedHash
    }
}