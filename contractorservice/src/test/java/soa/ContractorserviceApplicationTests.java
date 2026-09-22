package soa;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import soa.service.JwtService;

@SpringBootTest
class ContractorserviceApplicationTests {

	@Autowired(required = false)
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	void testJwtTokenGenerationAndValidation() {
		if (jwtService != null) {
			String token = jwtService.generateToken("testContractor", "CONTRACTOR", "Apex Builders");
			assertNotNull(token);
			assertTrue(jwtService.isTokenValid(token));
		}
	}
}
