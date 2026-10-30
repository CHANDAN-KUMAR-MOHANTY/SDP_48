package soa.controller;

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import soa.service.GatewayService;

@RestController
@RequestMapping("/")
public class GatewayController {

	private final GatewayService GS;

	public GatewayController(GatewayService GS) {
		this.GS = GS;
	}

	@GetMapping
	public Object info() {
		return Map.of(
			"system", "BuildAsset Logistics Enterprise Gateway",
			"status", "UP",
			"routing", "Dynamic Eureka Registry Routing with Round-Robin Load Balancing",
			"services", new String[]{"contractorservice", "equipmentservice", "rentalservice", "dispatchservice"}
		);
	}

	@GetMapping("{serviceName}/**")
	public Object forwardGet(
			@PathVariable String serviceName,
			HttpServletRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		String fullPath = request.getRequestURI();
		String prefix = "/" + serviceName;
		String endpoint = fullPath.substring(prefix.length());

		return GS.invokeService(HttpMethod.GET, serviceName, endpoint, null, authHeader);
	}

	@PostMapping("{serviceName}/**")
	public Object forwardPost(
			@PathVariable String serviceName,
			@RequestBody(required = false) Object data,
			HttpServletRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		String fullPath = request.getRequestURI();
		String prefix = "/" + serviceName;
		String endpoint = fullPath.substring(prefix.length());

		return GS.invokeService(HttpMethod.POST, serviceName, endpoint, data, authHeader);
	}

	@PutMapping("{serviceName}/**")
	public Object forwardPut(
			@PathVariable String serviceName,
			@RequestBody(required = false) Object data,
			HttpServletRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		String fullPath = request.getRequestURI();
		String prefix = "/" + serviceName;
		String endpoint = fullPath.substring(prefix.length());

		return GS.invokeService(HttpMethod.PUT, serviceName, endpoint, data, authHeader);
	}

	@org.springframework.web.bind.annotation.DeleteMapping("{serviceName}/**")
	public Object forwardDelete(
			@PathVariable String serviceName,
			HttpServletRequest request,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		String fullPath = request.getRequestURI();
		String prefix = "/" + serviceName;
		String endpoint = fullPath.substring(prefix.length());

		return GS.invokeService(HttpMethod.DELETE, serviceName, endpoint, null, authHeader);
	}
}
