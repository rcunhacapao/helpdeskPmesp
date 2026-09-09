package pmesp.helpdesk37bpmm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"DATABASE_URL=jdbc:h2:mem:helpdesk_context_test;DB_CLOSE_DELAY=-1",
		"DATABASE_USERNAME=sa",
		"DATABASE_PASSWORD="
})
class Helpdesk37bpmmApplicationTests {

	@Test
	void contextLoads() {
	}

}
