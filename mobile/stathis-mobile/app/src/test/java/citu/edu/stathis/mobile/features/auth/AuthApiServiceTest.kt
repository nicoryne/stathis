package citu.edu.stathis.mobile.features.auth

import citu.edu.stathis.mobile.features.auth.data.models.LoginRequest
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.domain.AuthApiService
import citu.edu.stathis.mobile.network.RetrofitTestUtils
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.create

class AuthApiServiceTest {
    private lateinit var service: AuthApiService
    private val server = RetrofitTestUtils.createMockServer()

    @Before
    fun setup() {
        server.start()
        val retrofit = RetrofitTestUtils.createRetrofit(server.url("/").toString())
        service = retrofit.create()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_returns_tokens() = runTest {
        val body = """
            {"accessToken":"acc","refreshToken":"ref"}
        """.trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val response: LoginResponse = service.login(LoginRequest(email = "e@example.com", password = "pw"))

        assertEquals("acc", response.accessToken)
        assertEquals("ref", response.refreshToken)
    }
}


