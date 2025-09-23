package citu.edu.stathis.mobile.features.exercise

import citu.edu.stathis.mobile.features.exercise.domain.ExerciseApiService
import citu.edu.stathis.mobile.network.RetrofitTestUtils
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.create

class ExerciseApiServiceTest {
    private lateinit var service: ExerciseApiService
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
    fun getAvailableExercises_returns_list() = runTest {
        val body = """
            [
              {"id":"e1","name":"Push Up","description":"desc","instructions":["Do it"],"type":"STRENGTH","targetMuscles":["Chest"],"difficulty":"BEGINNER"}
            ]
        """.trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val resp = service.getAvailableExercises(userId = "u1")
        val list = resp.body()!!
        assertEquals(1, list.size)
        assertEquals("e1", list[0].id)
    }
}


