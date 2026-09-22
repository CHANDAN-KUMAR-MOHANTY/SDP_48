package soa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import soa.model.Contractor;
import soa.model.LoginRequest;
import soa.repo.ContractorRepo;

@Service
public class ContractorService {

	private final ContractorRepo CR;
	private final JwtService JS;
	private final PasswordEncoder PE;

	public ContractorService(ContractorRepo CR, JwtService JS, PasswordEncoder PE) {
		this.CR = CR;
		this.JS = JS;
		this.PE = PE;
	}

	public Object signup(Contractor contractor) {
		Map<String, Object> response = new HashMap<>();

		if (contractor.getUsername() == null || contractor.getUsername().isBlank() ||
			contractor.getPassword() == null || contractor.getPassword().isBlank() ||
			contractor.getEmail() == null || contractor.getEmail().isBlank() ||
			contractor.getCompanyName() == null || contractor.getCompanyName().isBlank() ||
			contractor.getLicenseNumber() == null || contractor.getLicenseNumber().isBlank()) {

			response.put("code", 400);
			response.put("status", "Validation Failed");
			response.put("message", "Required contractor fields are missing (username, password, email, companyName, licenseNumber)");
			return response;
		}

		if (CR.existsByUsername(contractor.getUsername())) {
			response.put("code", 409);
			response.put("status", "Duplicate Error");
			response.put("message", "Username already exists: " + contractor.getUsername());
			return response;
		}

		if (CR.existsByEmail(contractor.getEmail())) {
			response.put("code", 409);
			response.put("status", "Duplicate Error");
			response.put("message", "Email already registered: " + contractor.getEmail());
			return response;
		}

		if (CR.existsByLicenseNumber(contractor.getLicenseNumber())) {
			response.put("code", 409);
			response.put("status", "Duplicate Error");
			response.put("message", "Business license number already in use: " + contractor.getLicenseNumber());
			return response;
		}

		try {
			// BCrypt encode password
			contractor.setPassword(PE.encode(contractor.getPassword()));

			if (contractor.getRole() == null || contractor.getRole().isBlank()) {
				contractor.setRole("CONTRACTOR");
			}

			Contractor saved = CR.save(contractor);

			response.put("code", 200);
			response.put("status", "Contractor Registered Successfully");
			response.put("id", saved.getId());
			response.put("username", saved.getUsername());
			response.put("companyName", saved.getCompanyName());
			response.put("role", saved.getRole());

		} catch (Exception e) {
			response.put("code", 500);
			response.put("status", "Registration Error");
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object login(LoginRequest loginRequest) {
		Map<String, Object> response = new HashMap<>();

		if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
			response.put("code", 400);
			response.put("status", "Bad Request");
			response.put("message", "Username and password must be provided");
			return response;
		}

		try {
			Contractor contractor = CR.findByUsername(loginRequest.getUsername());

			if (contractor == null) {
				response.put("code", 404);
				response.put("status", "Not Found");
				response.put("message", "Contractor not found with username: " + loginRequest.getUsername());
				return response;
			}

			if (!PE.matches(loginRequest.getPassword(), contractor.getPassword())) {
				response.put("code", 401);
				response.put("status", "Unauthorized");
				response.put("message", "Invalid username or password");
				return response;
			}

			String token = JS.generateToken(contractor.getUsername(), contractor.getRole(), contractor.getCompanyName());

			response.put("code", 200);
			response.put("status", "Login Successful");
			response.put("username", contractor.getUsername());
			response.put("companyName", contractor.getCompanyName());
			response.put("role", contractor.getRole());
			response.put("token", token);

		} catch (Exception e) {
			response.put("code", 500);
			response.put("status", "Login Error");
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object getProfile(String username) {
		Map<String, Object> response = new HashMap<>();

		try {
			Contractor contractor = CR.findByUsername(username);
			if (contractor == null) {
				response.put("code", 404);
				response.put("message", "Contractor not found");
				return response;
			}

			Map<String, Object> profile = new HashMap<>();
			profile.put("id", contractor.getId());
			profile.put("companyName", contractor.getCompanyName());
			profile.put("licenseNumber", contractor.getLicenseNumber());
			profile.put("username", contractor.getUsername());
			profile.put("email", contractor.getEmail());
			profile.put("role", contractor.getRole());
			profile.put("contactPhone", contractor.getContactPhone());
			profile.put("officeAddress", contractor.getOfficeAddress());

			response.put("code", 200);
			response.put("data", profile);

		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object getAllContractors() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Contractor> contractors = CR.findAll();
			// Remove password before returning
			contractors.forEach(c -> c.setPassword("PROTECTED"));
			response.put("code", 200);
			response.put("data", contractors);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}
}
