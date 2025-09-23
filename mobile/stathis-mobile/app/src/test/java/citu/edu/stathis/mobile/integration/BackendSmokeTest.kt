package citu.edu.stathis.mobile.integration

import citu.edu.stathis.mobile.features.auth.data.models.LoginRequest
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.domain.AuthApiService
import cit.edu.stathis.mobile.BuildConfig
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assume.assumeTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class BackendSmokeTest {
    private fun buildRetrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .client(client)
            .build()
    }

    @Test
    fun login_smoke() {
        val baseUrl = System.getenv("STATHIS_API_BASE_URL") ?: BuildConfig.API_BASE_URL
        val email = System.getProperty("stathis.test.email") ?: System.getenv("STATHIS_TEST_EMAIL")
        val password = System.getProperty("stathis.test.password") ?: System.getenv("STATHIS_TEST_PASSWORD")

        assumeTrue("Skipping: test creds not set", !email.isNullOrBlank() && !password.isNullOrBlank())

        val retrofit = buildRetrofit(baseUrl)
        val auth = retrofit.create<AuthApiService>()

        val resp: LoginResponse = kotlinx.coroutines.runBlocking {
            auth.login(LoginRequest(email = email!!, password = password!!))
        }

        // Basic sanity check
        assert(resp.accessToken.isNotBlank())
        assert(resp.refreshToken.isNotBlank())
    }
}


