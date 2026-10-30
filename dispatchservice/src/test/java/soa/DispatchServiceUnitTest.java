package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import soa.model.DispatchRecord;
import soa.repo.DispatchRepo;
import soa.service.DispatchService;

@ExtendWith(MockitoExtension.class)
public class DispatchServiceUnitTest {

	@Mock
	private DispatchRepo dispatchRepo;

	@Mock
	private DiscoveryClient discoveryClient;

	private DispatchService dispatchService;

	@BeforeEach
	void setUp() {
		dispatchService = new DispatchService(dispatchRepo, discoveryClient);
	}

	@Test
	@DisplayName("Negative: Rejects dispatch record with missing required booking reference or site")
	void testMissingFieldsValidation() {
		DispatchRecord record = new DispatchRecord();
		record.setEquipmentId(1L);

		Object res = dispatchService.scheduleDispatch(record);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(400, respMap.get("code"));
		assertEquals("Validation Failed", respMap.get("status"));
	}

	@Test
	@DisplayName("Positive: Successfully updates tracking status to IN_TRANSIT")
	void testStatusUpdate() {
		DispatchRecord record = new DispatchRecord();
		record.setId(1L);
		record.setEquipmentId(10L);
		record.setSiteLocation("Metro Site");
		record.setDispatchStatus("SCHEDULED");
		record.setDispatchTrackingNumber("TRK-9871");

		when(dispatchRepo.findById(1L)).thenReturn(Optional.of(record));
		when(dispatchRepo.save(any(DispatchRecord.class))).thenAnswer(inv -> inv.getArgument(0));

		Object res = dispatchService.updateDispatchStatus(1L, "IN_TRANSIT");
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertEquals("IN_TRANSIT", respMap.get("currentStatus"));
		assertEquals("TRK-9871", respMap.get("dispatchTrackingNumber"));
	}

	@Test
	@DisplayName("Negative: Returns 404 when updating non-existent dispatch")
	void testUpdateNotFound() {
		when(dispatchRepo.findById(888L)).thenReturn(Optional.empty());

		Object res = dispatchService.updateDispatchStatus(888L, "DELIVERED_ON_SITE");
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(404, respMap.get("code"));
		assertTrue(respMap.get("message").toString().contains("Dispatch record not found"));
	}
}
