package tn.esprit.dam.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.di.NetworkModule
import java.io.IOException

class NetworkClientTest {

    @Serializable
    data class Item(val id: Int)

    private val tokens = FakeTokenRepository(access = "old-access", refresh = "refresh-1")
    private val calls = mutableListOf<HttpRequestData>()

    private fun client(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
        val engine = MockEngine { request ->
            calls += request
            handler(request)
        }
        return NetworkModule.buildHttpClient(engine, tokens, retryBackoff = false)
    }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun pathCalls(path: String) = calls.count { it.url.encodedPath == path }

    // --- retry policy -------------------------------------------------------------------------

    @Test
    fun `GET is retried on 5xx until it succeeds`() = runBlocking {
        var attempt = 0
        val http = client { if (++attempt < 3) json("{}", HttpStatusCode.InternalServerError) else json("""{"id":1}""") }

        val result = safeApiCall<Item> { http.get("/api/items/1") }

        assertEquals(ApiResult.Success(Item(1)), result)
        assertEquals(3, pathCalls("/api/items/1"))
    }

    @Test
    fun `POST is not retried on 5xx`() = runBlocking {
        val http = client { json("""{"message":"boom"}""", HttpStatusCode.InternalServerError) }

        val result = safeApiCall<Item> { http.post("/api/items") }

        assertEquals(ApiResult.ApiError("boom", 500), result)
        assertEquals(1, pathCalls("/api/items"))
    }

    @Test
    fun `POST is not retried on network failure`() = runBlocking {
        val http = client { throw IOException("offline") }

        val result = safeApiCall<Item> { http.post("/api/items") }

        assertTrue(result is ApiResult.NetworkError)
        assertEquals(1, pathCalls("/api/items"))
    }

    // --- 401 handled by the Auth plugin only -------------------------------------------------

    @Test
    fun `401 refreshes the token once and replays the request`() = runBlocking {
        val http = client { request ->
            when (request.url.encodedPath) {
                "/api/auth/refresh" -> json(
                    """{"user":{"email":"a@b.c","name":"A"},"accessToken":"new-access","refreshToken":"refresh-2"}"""
                )
                else -> if (request.headers[HttpHeaders.Authorization] == "Bearer new-access") {
                    json("""{"id":7}""")
                } else {
                    json("{}", HttpStatusCode.Unauthorized)
                }
            }
        }

        val result = safeApiCall<Item> { http.get("/api/items/7") }

        assertEquals(ApiResult.Success(Item(7)), result)
        assertEquals(1, pathCalls("/api/auth/refresh"))
        assertEquals("new-access", tokens.access)
        assertEquals("refresh-2", tokens.refresh)
    }

    @Test
    fun `rejected refresh clears the session`() = runBlocking {
        val http = client { request ->
            when (request.url.encodedPath) {
                "/api/auth/refresh" -> json("""{"message":"expired"}""", HttpStatusCode.Unauthorized)
                else -> json("""{"message":"Unauthorized"}""", HttpStatusCode.Unauthorized)
            }
        }

        val result = safeApiCall<Item> { http.get("/api/items/7") }

        assertEquals(ApiResult.ApiError("Unauthorized", 401), result)
        assertNull(tokens.access)
        assertNull(tokens.refresh)
    }

    @Test
    fun `offline refresh keeps the session`() = runBlocking {
        val http = client { request ->
            when (request.url.encodedPath) {
                "/api/auth/refresh" -> throw IOException("offline")
                else -> json("{}", HttpStatusCode.Unauthorized)
            }
        }

        safeApiCall<Item> { http.get("/api/items/7") }

        assertEquals("old-access", tokens.access)
    }

    @Test
    fun `401 on login is a credentials error, not a refresh`() = runBlocking {
        val http = client { json("""{"message":"Invalid credentials"}""", HttpStatusCode.Unauthorized) }

        val result = safeApiCall<Item> { http.post("/api/auth/login") }

        assertEquals(ApiResult.ApiError("Invalid credentials", 401), result)
        assertEquals(0, pathCalls("/api/auth/refresh"))
        assertEquals("old-access", tokens.access)
    }

    // --- safeApiCall mapping ------------------------------------------------------------------

    @Test
    fun `token is attached to every request`() = runBlocking {
        val http = client { json("""{"id":1}""") }

        safeApiCall<Item> { http.get("/api/items/1") }

        assertEquals("Bearer old-access", calls.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun `malformed body is a SerializationError`() = runBlocking {
        val http = client { json("""{"unexpected":true}""") }

        val result = safeApiCall<Item> { http.get("/api/items/1") }

        assertTrue(result is ApiResult.SerializationError)
    }

    @Test
    fun `non-json error body falls back to raw text`() = runBlocking {
        val http = client { respond("Bad Gateway", HttpStatusCode.BadGateway) }

        val result = safeApiCall<Item> { http.post("/api/items") }

        assertEquals(ApiResult.ApiError("Bad Gateway", 502), result)
    }

    private class FakeTokenRepository(var access: String?, var refresh: String?) : TokenRepository {
        override suspend fun saveTokens(accessToken: String, refreshToken: String) {
            access = accessToken; refresh = refreshToken
        }
        override suspend fun getAccessToken() = access
        override suspend fun getRefreshToken() = refresh
        override fun observeAccessToken(): Flow<String?> = MutableStateFlow(access)
        override suspend fun clearTokens() {
            access = null; refresh = null
        }
        override suspend fun hasTokens() = access != null && refresh != null
        override suspend fun saveUser(user: User) = Unit
        override suspend fun saveUserData(userData: String) = Unit
        override suspend fun getUser(): User? = null
        override suspend fun getUserData(): String? = null
        override fun getUserFlow(): Flow<User?> = MutableStateFlow(null)
        override suspend fun setLoggedIn(isLoggedIn: Boolean) = Unit
        override suspend fun isLoggedIn() = access != null
        override fun isLoggedInFlow(): Flow<Boolean> = MutableStateFlow(access != null)
    }
}
