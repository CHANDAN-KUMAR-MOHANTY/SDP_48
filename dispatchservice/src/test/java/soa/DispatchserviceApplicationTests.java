package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import soa.model.DispatchRecord;

@SpringBootTest
class DispatchserviceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testDispatchStatusTransitions() {
		DispatchRecord record = new DispatchRecord();
		record.setDispatchStatus("SCHEDULED");
		record.setSiteLocation("Metro Rail Pier 44");
		record.setDispatchTrackingNumber("TRK-DISP-1001");

		assertEquals("SCHEDULED", record.getDispatchStatus());
		record.setDispatchStatus("IN_TRANSIT");
		assertEquals("IN_TRANSIT", record.getDispatchStatus());
		record.setDispatchStatus("DELIVERED_ON_SITE");
		assertEquals("DELIVERED_ON_SITE", record.getDispatchStatus());
		assertNotNull(record);
	}
}
