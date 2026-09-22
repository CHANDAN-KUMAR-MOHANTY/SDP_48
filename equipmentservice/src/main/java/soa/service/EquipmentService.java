package soa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import soa.model.Equipment;
import soa.repo.EquipmentRepo;

@Service
public class EquipmentService {

	private final EquipmentRepo ER;

	public EquipmentService(EquipmentRepo ER) {
		this.ER = ER;
	}

	public Object addEquipment(Equipment equipment) {
		Map<String, Object> response = new HashMap<>();

		if (equipment.getEquipmentName() == null || equipment.getEquipmentName().isBlank() ||
			equipment.getCategory() == null || equipment.getCategory().isBlank() ||
			equipment.getModelNumber() == null || equipment.getModelNumber().isBlank() ||
			equipment.getDailyRate() == null) {

			response.put("code", 400);
			response.put("status", "Validation Failed");
			response.put("message", "Missing required fields: equipmentName, category, modelNumber, dailyRate");
			return response;
		}

		if (equipment.getStatus() == null || equipment.getStatus().isBlank()) {
			equipment.setStatus("AVAILABLE");
		}

		if (equipment.getCurrentLocation() == null || equipment.getCurrentLocation().isBlank()) {
			equipment.setCurrentLocation("Central Depot Yard");
		}

		try {
			Equipment saved = ER.save(equipment);
			response.put("code", 200);
			response.put("status", "Equipment Cataloged Successfully");
			response.put("id", saved.getId());
			response.put("data", saved);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("status", "Cataloging Error");
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object getAllEquipment() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Equipment> list = ER.findAll();
			response.put("code", 200);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}

	public Object getEquipmentById(Long id) {
		Map<String, Object> response = new HashMap<>();
		Optional<Equipment> opt = ER.findById(id);
		if (opt.isEmpty()) {
			response.put("code", 404);
			response.put("message", "Equipment not found with ID: " + id);
			return response;
		}
		response.put("code", 200);
		response.put("data", opt.get());
		return response;
	}

	public Object getByCategory(String category) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Equipment> list = ER.findByCategory(category.toUpperCase());
			response.put("code", 200);
			response.put("category", category);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}

	public Object getByStatus(String status) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<Equipment> list = ER.findByStatus(status.toUpperCase());
			response.put("code", 200);
			response.put("statusFilter", status);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}

	public Object updateStatus(Long id, String status, String location) {
		Map<String, Object> response = new HashMap<>();
		Optional<Equipment> opt = ER.findById(id);
		if (opt.isEmpty()) {
			response.put("code", 404);
			response.put("message", "Equipment not found with ID: " + id);
			return response;
		}

		Equipment eq = opt.get();
		eq.setStatus(status.toUpperCase());
		if (location != null && !location.isBlank()) {
			eq.setCurrentLocation(location);
		}

		try {
			Equipment updated = ER.save(eq);
			response.put("code", 200);
			response.put("status", "Equipment Status Updated Successfully");
			response.put("id", updated.getId());
			response.put("currentStatus", updated.getStatus());
			response.put("currentLocation", updated.getCurrentLocation());
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object deleteEquipment(Long id) {
		Map<String, Object> response = new HashMap<>();
		if (!ER.existsById(id)) {
			response.put("code", 404);
			response.put("message", "Equipment not found with ID: " + id);
			return response;
		}
		try {
			ER.deleteById(id);
			response.put("code", 200);
			response.put("status", "Equipment Deleted Successfully");
			response.put("id", id);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}
}
