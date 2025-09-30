package citu.edu.stathis.mobile.integration

import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.data.models.RegisterRequest
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
import java.util.UUID

class BackendRegistrationTest {
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
    fun register_connectivity_smoke() {
        val baseUrl = System.getenv("STATHIS_API_BASE_URL") ?: BuildConfig.API_BASE_URL

        // Optional gate: only run when explicitly enabled
        val run = System.getenv("STATHIS_ENABLE_REGISTRATION_TEST")?.toBoolean() == true
        assumeTrue("Skipping registration connectivity test (set STATHIS_ENABLE_REGISTRATION_TEST=true)", run)

        val retrofit = buildRetrofit(baseUrl)
        val auth = retrofit.create<AuthApiService>()

        val uniqueEmail = "regtest+${UUID.randomUUID()}@example.com"
        val request = RegisterRequest(
            email = uniqueEmail,
            password = "Testpass1!",
            firstName = "Test",
            lastName = "User",
            userRole = UserRoles.STUDENT
        )

        kotlinx.coroutines.runBlocking {
            auth.register(request)
        }

        // If no exception is thrown, basic connectivity is OK
        assert(true)
    }
}


