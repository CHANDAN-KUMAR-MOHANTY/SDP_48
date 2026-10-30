package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import soa.model.Contractor;
import soa.model.LoginRequest;
import soa.repo.ContractorRepo;
import soa.service.ContractorService;
import soa.service.JwtService;

@ExtendWith(MockitoExtension.class)
public class ContractorServiceUnitTest {

	@Mock
	private ContractorRepo contractorRepo;

	@Mock
	private JwtService jwtService;

	@Mock
	private PasswordEncoder passwordEncoder;

	private ContractorService contractorService;

	@BeforeEach
	void setUp() {
		contractorService = new ContractorService(contractorRepo, jwtService, passwordEncoder);
	}

	@Test
	@DisplayName("Positive: Successfully registers new contractor")
	void testSuccessfulSignup() {
		Contractor contractor = new Contractor();
		contractor.setUsername("alpha_builder");
		contractor.setPassword("Pass@1234");
		contractor.setEmail("contact@alphabuild.com");
		contractor.setCompanyName("Alpha Builders Ltd");
		contractor.setLicenseNumber("LIC-IN-9876");

		when(contractorRepo.existsByUsername("alpha_builder")).thenReturn(false);
		when(contractorRepo.existsByEmail("contact@alphabuild.com")).thenReturn(false);
		when(contractorRepo.existsByLicenseNumber("LIC-IN-9876")).thenReturn(false);
		when(passwordEncoder.encode("Pass@1234")).thenReturn("hashedPassword123");
		when(contractorRepo.save(any(Contractor.class))).thenAnswer(invocation -> {
			Contractor c = invocation.getArgument(0);
			c.setId(101L);
			return c;
		});

		Object res = contractorService.signup(contractor);
		assertTrue(res instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertEquals("Contractor Registered Successfully", respMap.get("status"));
		assertEquals("alpha_builder", respMap.get("username"));
	}

	@Test
	@DisplayName("Negative: Signup fails when required fields are missing")
	void testSignupMissingFields() {
		Contractor contractor = new Contractor();
		contractor.setUsername("incomplete_user");

		Object res = contractorService.signup(contractor);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(400, respMap.get("code"));
		assertEquals("Validation Failed", respMap.get("status"));
	}

	@Test
	@DisplayName("Negative: Signup fails on duplicate username")
	void testSignupDuplicateUsername() {
		Contractor contractor = new Contractor();
		contractor.setUsername("existing_user");
		contractor.setPassword("Pass@123");
		contractor.setEmail("user@test.com");
		contractor.setCompanyName("Existing Ltd");
		contractor.setLicenseNumber("LIC-0001");

		when(contractorRepo.existsByUsername("existing_user")).thenReturn(true);

		Object res = contractorService.signup(contractor);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(409, respMap.get("code"));
		assertTrue(respMap.get("message").toString().contains("Username already exists"));
	}

	@Test
	@DisplayName("Negative: Signup fails on duplicate license number")
	void testSignupDuplicateLicense() {
		Contractor contractor = new Contractor();
		contractor.setUsername("new_user");
		contractor.setPassword("Pass@123");
		contractor.setEmail("user2@test.com");
		contractor.setCompanyName("New Ltd");
		contractor.setLicenseNumber("LIC-EXISTING");

		when(contractorRepo.existsByUsername("new_user")).thenReturn(false);
		when(contractorRepo.existsByEmail("user2@test.com")).thenReturn(false);
		when(contractorRepo.existsByLicenseNumber("LIC-EXISTING")).thenReturn(true);

		Object res = contractorService.signup(contractor);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(409, respMap.get("code"));
		assertTrue(respMap.get("message").toString().contains("Business license number already in use"));
	}

	@Test
	@DisplayName("Positive: Successfully authenticates contractor and generates JWT")
	void testSuccessfulLogin() {
		LoginRequest req = new LoginRequest("validUser", "securePassword");

		Contractor saved = new Contractor();
		saved.setUsername("validUser");
		saved.setPassword("hashedPassword");
		saved.setRole("CONTRACTOR");
		saved.setCompanyName("Apex Constructors");

		when(contractorRepo.findByUsername("validUser")).thenReturn(saved);
		when(passwordEncoder.matches("securePassword", "hashedPassword")).thenReturn(true);
		when(jwtService.generateToken("validUser", "CONTRACTOR", "Apex Constructors")).thenReturn("jwt.token.sample");

		Object res = contractorService.login(req);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertEquals("Login Successful", respMap.get("status"));
		assertEquals("jwt.token.sample", respMap.get("token"));
	}

	@Test
	@DisplayName("Negative: Login fails with invalid password")
	void testLoginInvalidPassword() {
		LoginRequest req = new LoginRequest("validUser", "wrongPassword");

		Contractor saved = new Contractor();
		saved.setUsername("validUser");
		saved.setPassword("hashedPassword");

		when(contractorRepo.findByUsername("validUser")).thenReturn(saved);
		when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

		Object res = contractorService.login(req);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(401, respMap.get("code"));
		assertEquals("Unauthorized", respMap.get("status"));
	}

	@Test
	@DisplayName("Negative: Login fails for non-existent user")
	void testLoginNonExistentUser() {
		LoginRequest req = new LoginRequest("unknownUser", "anyPassword");

		when(contractorRepo.findByUsername("unknownUser")).thenReturn(null);

		Object res = contractorService.login(req);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(404, respMap.get("code"));
		assertEquals("Not Found", respMap.get("status"));
	}

	@Test
	@DisplayName("Validates JWT generation and claim extraction")
	void testJwtLifecycle() {
		JwtService realJwtService = new JwtService();
		String token = realJwtService.generateToken("contractor_john", "CONTRACTOR", "InfraBuild");

		assertNotNull(token);
		assertTrue(realJwtService.isTokenValid(token));
		assertEquals("contractor_john", realJwtService.extractUsername(token));
		assertEquals("CONTRACTOR", realJwtService.extractRole(token));
		assertEquals("InfraBuild", realJwtService.extractCompanyName(token));
	}

	@Test
	@DisplayName("Negative: Rejects tampered/malformed JWT token")
	void testTamperedJwt() {
		JwtService realJwtService = new JwtService();
		String malformedToken = "ey12345.malformed.tokenValue";

		assertFalse(realJwtService.isTokenValid(malformedToken));
	}
}
