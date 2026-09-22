package soa.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GatewayService {

	private final DiscoveryClient DC;
	private final Map<String, Integer> instanceCounters = new ConcurrentHashMap<>();

	public GatewayService(DiscoveryClient DC) {
		this.DC = DC;
	}

	public Object invokeService(HttpMethod method, String serviceName, String endpoint, Object data, String authHeader) {
		List<ServiceInstance> services = DC.getInstances(serviceName.toUpperCase());

		if (services == null || services.isEmpty()) {
			services = DC.getInstances(serviceName.toLowerCase());
		}

		if (services == null || services.isEmpty()) {
			return Map.of(
				"code", 503,
				"status", "Service Unavailable",
				"message", "Service [" + serviceName + "] is currently offline or not registered with Eureka Server"
			);
		}

		int current = instanceCounters.getOrDefault(serviceName, 0);
		if (current >= services.size()) {
			current = 0;
		}

		ServiceInstance instance = services.get(current);
		instanceCounters.put(serviceName, (current + 1) % services.size());

		String cleanEndpoint = endpoint.startsWith("/") ? endpoint.substring(1) : endpoint;
		String targetUrl = String.format("%s/%s", instance.getUri(), cleanEndpoint);

		System.out.println("[Gateway LoadBalancer] Dispatching (" + method + ") to: " + targetUrl);

		RestClient client = RestClient.create();
		RestClient.RequestBodySpec spec;

		if (method == HttpMethod.POST) {
			spec = client.post().uri(targetUrl);
		} else if (method == HttpMethod.PUT) {
			spec = client.put().uri(targetUrl);
		} else {
			spec = client.method(method).uri(targetUrl);
		}

		if (authHeader != null && !authHeader.isBlank()) {
			spec.header("Authorization", authHeader);
		}

		if (data != null) {
			spec.contentType(MediaType.APPLICATION_JSON).body(data);
		}

		try {
			return spec.retrieve().body(String.class);
		} catch (Exception e) {
			return Map.of(
				"code", 500,
				"status", "Gateway Dispatch Error",
				"message", e.getMessage()
			);
		}
	}
}
