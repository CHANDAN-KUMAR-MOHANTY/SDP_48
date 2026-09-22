package soa.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import soa.model.Equipment;
import soa.service.EquipmentService;

@RestController
@RequestMapping("api")
public class EquipmentController {

	private final EquipmentService ES;

	public EquipmentController(EquipmentService ES) {
		this.ES = ES;
	}

	@PostMapping("equipment")
	public Object addEquipment(@RequestBody Equipment equipment) {
		return ES.addEquipment(equipment);
	}

	@GetMapping("equipment")
	public Object getAllEquipment() {
		return ES.getAllEquipment();
	}

	@GetMapping("equipment/{id}")
	public Object getEquipmentById(@PathVariable Long id) {
		return ES.getEquipmentById(id);
	}

	@GetMapping("equipment/category/{category}")
	public Object getByCategory(@PathVariable String category) {
		return ES.getByCategory(category);
	}

	@GetMapping("equipment/status/{status}")
	public Object getByStatus(@PathVariable String status) {
		return ES.getByStatus(status);
	}

	@PutMapping("equipment/{id}/status")
	public Object updateStatus(
			@PathVariable Long id,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String location,
			@RequestBody(required = false) Map<String, String> body) {

		String newStatus = status;
		String newLocation = location;

		if (body != null) {
			if (newStatus == null && body.containsKey("status")) {
				newStatus = body.get("status");
			}
			if (newLocation == null && body.containsKey("location")) {
				newLocation = body.get("location");
			}
		}

		return ES.updateStatus(id, newStatus, newLocation);
	}

	@DeleteMapping("equipment/{id}")
	public Object deleteEquipment(@PathVariable Long id) {
		return ES.deleteEquipment(id);
	}
}
