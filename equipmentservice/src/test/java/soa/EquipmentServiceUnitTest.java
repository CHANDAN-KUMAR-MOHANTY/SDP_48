package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import soa.model.Equipment;
import soa.repo.EquipmentRepo;
import soa.service.EquipmentService;

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceUnitTest {

	@Mock
	private EquipmentRepo equipmentRepo;

	private EquipmentService equipmentService;

	@BeforeEach
	void setUp() {
		equipmentService = new EquipmentService(equipmentRepo);
	}

	@Test
	@DisplayName("Positive: Successfully registers new heavy equipment")
	void testSuccessfulEquipmentAddition() {
		Equipment eq = new Equipment();
		eq.setEquipmentName("Komatsu PC210 Hydraulic Excavator");
		eq.setCategory("EXCAVATOR");
		eq.setModelNumber("KOM-PC210");
		eq.setDailyRate(650.0);
		eq.setStatus("AVAILABLE");

		when(equipmentRepo.save(any(Equipment.class))).thenAnswer(inv -> {
			Equipment saved = inv.getArgument(0);
			saved.setId(10L);
			return saved;
		});

		Object res = equipmentService.addEquipment(eq);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertEquals("Equipment Added Successfully", respMap.get("status"));
		assertNotNull(respMap.get("equipment"));
	}

	@Test
	@DisplayName("Negative: Rejects equipment with missing name or invalid rate")
	void testMissingEquipmentDetails() {
		Equipment eq = new Equipment();
		eq.setDailyRate(-100.0);

		Object res = equipmentService.addEquipment(eq);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(400, respMap.get("code"));
		assertEquals("Validation Failed", respMap.get("status"));
	}

	@Test
	@DisplayName("Positive: Finds equipment by ID")
	void testFindEquipmentById() {
		Equipment eq = new Equipment();
		eq.setId(5L);
		eq.setEquipmentName("Caterpillar D6T Bulldozer");
		eq.setStatus("AVAILABLE");

		when(equipmentRepo.findById(5L)).thenReturn(Optional.of(eq));

		Object res = equipmentService.getEquipmentById(5L);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertNotNull(respMap.get("data"));
	}

	@Test
	@DisplayName("Negative: Returns 404 when equipment does not exist")
	void testEquipmentNotFound() {
		when(equipmentRepo.findById(999L)).thenReturn(Optional.empty());

		Object res = equipmentService.getEquipmentById(999L);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(404, respMap.get("code"));
		assertTrue(respMap.get("message").toString().contains("Equipment not found"));
	}

	@Test
	@DisplayName("Positive: Updates equipment status to RENTED")
	void testUpdateEquipmentStatus() {
		Equipment eq = new Equipment();
		eq.setId(1L);
		eq.setStatus("AVAILABLE");

		when(equipmentRepo.findById(1L)).thenReturn(Optional.of(eq));
		when(equipmentRepo.save(any(Equipment.class))).thenAnswer(inv -> inv.getArgument(0));

		Object res = equipmentService.updateStatus(1L, "RENTED", "Metro Rail Site B");
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertEquals("RENTED", respMap.get("newStatus"));
	}
}
