package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import soa.model.Equipment;

@SpringBootTest
class EquipmentserviceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testEquipmentEntityProperties() {
		Equipment eq = new Equipment();
		eq.setEquipmentName("CAT 320 Excavator");
		eq.setCategory("EXCAVATOR");
		eq.setModelNumber("CAT-320-GC");
		eq.setDailyRate(500.0);
		eq.setStatus("AVAILABLE");

		assertNotNull(eq);
		assertEquals("CAT 320 Excavator", eq.getEquipmentName());
		assertEquals("AVAILABLE", eq.getStatus());
		assertEquals(500.0, eq.getDailyRate());
	}
}
