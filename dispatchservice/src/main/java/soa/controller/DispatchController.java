package soa.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import soa.model.DispatchRecord;
import soa.service.DispatchService;

@RestController
@RequestMapping("api")
public class DispatchController {

	private final DispatchService DS;

	public DispatchController(DispatchService DS) {
		this.DS = DS;
	}

	@PostMapping("dispatch/schedule")
	public Object scheduleDispatch(@RequestBody DispatchRecord dispatch) {
		return DS.scheduleDispatch(dispatch);
	}

	@GetMapping("dispatch/track/{trackingNumber}")
	public Object getTrackingDetails(@PathVariable String trackingNumber) {
		return DS.getTrackingDetails(trackingNumber);
	}

	@GetMapping("dispatch/booking/{bookingReference}")
	public Object getDispatchesByBooking(@PathVariable String bookingReference) {
		return DS.getDispatchesByBooking(bookingReference);
	}

	@GetMapping("dispatch")
	public Object getAllDispatches() {
		return DS.getAllDispatches();
	}

	@PutMapping("dispatch/{id}/status")
	public Object updateDispatchStatus(
			@PathVariable Long id,
			@RequestParam(required = false) String status,
			@RequestBody(required = false) Map<String, String> body) {

		String newStatus = status;
		if (newStatus == null && body != null && body.containsKey("status")) {
			newStatus = body.get("status");
		}

		if (newStatus == null || newStatus.isBlank()) {
			return Map.of("code", 400, "message", "Status parameter or JSON body { 'status': '...' } is required");
		}

		return DS.updateDispatchStatus(id, newStatus);
	}
}
