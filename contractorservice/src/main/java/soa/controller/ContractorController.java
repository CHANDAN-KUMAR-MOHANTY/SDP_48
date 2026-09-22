package soa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import soa.model.Contractor;
import soa.model.LoginRequest;
import soa.service.ContractorService;

@RestController
@RequestMapping("api")
public class ContractorController {

	private final ContractorService CS;

	public ContractorController(ContractorService CS) {
		this.CS = CS;
	}

	@PostMapping("auth/signup")
	public Object signup(@RequestBody Contractor contractor) {
		return CS.signup(contractor);
	}

	@PostMapping("auth/login")
	public Object login(@RequestBody LoginRequest loginRequest) {
		return CS.login(loginRequest);
	}

	@GetMapping("auth/profile/{username}")
	public Object getProfile(@PathVariable String username) {
		return CS.getProfile(username);
	}

	@GetMapping("contractors")
	public Object getAllContractors() {
		return CS.getAllContractors();
	}

	@GetMapping("contractors/{username}")
	public Object getContractorDetails(@PathVariable String username) {
		return CS.getProfile(username);
	}
}
