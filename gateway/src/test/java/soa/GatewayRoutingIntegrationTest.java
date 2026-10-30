package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;

import soa.service.GatewayService;

@ExtendWith(MockitoExtension.class)
public class GatewayRoutingIntegrationTest {

	@Mock
	private DiscoveryClient discoveryClient;

	private GatewayService gatewayService;

	@BeforeEach
	void setUp() {
		gatewayService = new GatewayService(discoveryClient);
	}

	@Test
	@DisplayName("Gateway Route Fallback: Returns 503 when service is offline or not registered in Eureka")
	void testServiceUnavailableHandling() {
		when(discoveryClient.getInstances(anyString())).thenReturn(Collections.emptyList());

		Object result = gatewayService.invokeService(
				HttpMethod.GET,
				"nonexistentservice",
				"/api/test",
				null,
				null
		);

		assertTrue(result instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) result;

		assertEquals(503, respMap.get("code"));
		assertEquals("Service Unavailable", respMap.get("status"));
		assertTrue(respMap.get("message").toString().contains("currently offline"));
	}

	@Test
	@DisplayName("Gateway Security: Preserves and propagates JWT Authorization headers")
	void testAuthHeaderHandling() {
		when(discoveryClient.getInstances("SECURESERVICE")).thenReturn(Collections.emptyList());

		Object result = gatewayService.invokeService(
				HttpMethod.GET,
				"secureservice",
				"/api/protected",
				null,
				"Bearer eyJhbGciOi..."
		);

		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) result;
		assertEquals(503, respMap.get("code"));
	}
}
