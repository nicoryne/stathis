package citu.edu.stathis.mobile.features.vitals

import citu.edu.stathis.mobile.features.vitals.domain.VitalsApiService
import citu.edu.stathis.mobile.network.RetrofitTestUtils
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.create

class VitalsApiServiceTest {
    private lateinit var service: VitalsApiService
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
    fun getVitalsHistory_returns_list() = runTest {
        val body = """
            [
              {"physicalId":"r1","studentId":"u1","classroomId":null,"taskId":null,"heartRate":72,"oxygenSaturation":98,"timestamp":"2024-01-01T00:00:00Z","isPreActivity":true,"isPostActivity":false,"bpSys":120,"bpDia":80,"temperature":36.5,"respirationRate":16}
            ]
        """.trimIndent()
        server.enqueue(MockResponse().setBody(body).setResponseCode(200))

        val resp = service.getVitalsHistory(userId = "u1")
        val list = resp.body()!!
        assertEquals(1, list.size)
        assertEquals("r1", list[0].physicalId)
    }
}


