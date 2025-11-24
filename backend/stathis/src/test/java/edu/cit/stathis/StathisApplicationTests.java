package edu.cit.stathis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.mock.mockito.MockBean;
import edu.cit.stathis.posture.service.PostureModelService;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class StathisApplicationTests {

	@MockBean
	private PostureModelService postureModelService;

	@Test
	void contextLoads() {
	}

}
