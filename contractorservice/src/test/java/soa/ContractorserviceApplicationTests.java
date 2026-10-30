package soa;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import soa.service.JwtService;

@SpringBootTest
class ContractorserviceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testJwtTokenGenerationAndValidation() {
		JwtService jwt = new JwtService();
		String token = jwt.generateToken("testContractor", "CONTRACTOR", "Apex Builders");
		assertNotNull(token);
		assertTrue(jwt.isTokenValid(token));
	}
}
