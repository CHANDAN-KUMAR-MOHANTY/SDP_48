package soa.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import soa.model.DispatchRecord;
import soa.repo.DispatchRepo;

@Service
public class DispatchService {

	private final DispatchRepo DR;
	private final DiscoveryClient DC;

	@Value("${gateway.url:http://localhost:8000}")
	private String gatewayUrl;

	public DispatchService(DispatchRepo DR, DiscoveryClient DC) {
		this.DR = DR;
		this.DC = DC;
	}

	private String getServiceBaseUrl(String serviceName) {
		try {
			List<ServiceInstance> instances = DC.getInstances(serviceName.toUpperCase());
			if (instances != null && !instances.isEmpty()) {
				return instances.get(0).getUri().toString();
			}
		} catch (Exception ignored) {}
		return gatewayUrl + "/" + serviceName.toLowerCase();
	}

	public Object scheduleDispatch(DispatchRecord dispatch) {
		Map<String, Object> response = new HashMap<>();

		if (dispatch.getBookingReference() == null || dispatch.getBookingReference().isBlank() ||
			dispatch.getEquipmentId() == null ||
			dispatch.getSiteLocation() == null || dispatch.getSiteLocation().isBlank() ||
			dispatch.getSiteSupervisorName() == null || dispatch.getSiteSupervisorName().isBlank() ||
			dispatch.getSupervisorContact() == null || dispatch.getSupervisorContact().isBlank()) {

			response.put("code", 400);
			response.put("status", "Validation Failed");
			response.put("message", "Missing required fields: bookingReference, equipmentId, siteLocation, siteSupervisorName, supervisorContact");
			return response;
		}

		dispatch.setDispatchTrackingNumber("TRK-DISP-" + (System.currentTimeMillis() % 1000000));
		dispatch.setDispatchStatus("SCHEDULED");
		dispatch.setDispatchDateTime(LocalDateTime.now().toString());

		try {
			DispatchRecord saved = DR.save(dispatch);

			// Workflow step: Update equipment status during dispatch execution
			try {
				String equipmentBase = getServiceBaseUrl("equipmentservice");
				String statusUpdateUrl = equipmentBase + "/api/equipment/" + dispatch.getEquipmentId() + "/status";
				RestClient client = RestClient.create();
				Map<String, String> statusBody = Map.of(
					"status", "IN_DISPATCH",
					"location", dispatch.getSiteLocation()
				);
				client.put()
						.uri(statusUpdateUrl)
						.contentType(MediaType.APPLICATION_JSON)
						.body(statusBody)
						.retrieve()
						.toBodilessEntity();
			} catch (Exception ex) {
				System.err.println("[DispatchService] Warning: Could not update equipment status: " + ex.getMessage());
			}

			response.put("code", 200);
			response.put("status", "Asset Dispatch Scheduled Successfully");
			response.put("dispatchTrackingNumber", saved.getDispatchTrackingNumber());
			response.put("bookingReference", saved.getBookingReference());
			response.put("siteLocation", saved.getSiteLocation());
			response.put("dispatchStatus", saved.getDispatchStatus());
			response.put("data", saved);

		} catch (Exception e) {
			response.put("code", 500);
			response.put("status", "Dispatch Scheduling Error");
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object updateDispatchStatus(Long id, String status) {
		Map<String, Object> response = new HashMap<>();

		Optional<DispatchRecord> opt = DR.findById(id);
		if (opt.isEmpty()) {
			response.put("code", 404);
			response.put("message", "Dispatch record not found with ID: " + id);
			return response;
		}

		DispatchRecord dispatch = opt.get();
		String upperStatus = status.toUpperCase();
		dispatch.setDispatchStatus(upperStatus);
		DR.save(dispatch);

		// Handle equipment status synchronization during dispatch execution
		try {
			String equipmentBase = getServiceBaseUrl("equipmentservice");
			String statusUpdateUrl = equipmentBase + "/api/equipment/" + dispatch.getEquipmentId() + "/status";
			RestClient client = RestClient.create();

			String targetEquipmentStatus = "IN_DISPATCH";
			String targetLocation = dispatch.getSiteLocation();

			if ("DELIVERED_ON_SITE".equalsIgnoreCase(upperStatus)) {
				targetEquipmentStatus = "DELIVERED_ON_SITE";
			} else if ("RETURNED".equalsIgnoreCase(upperStatus)) {
				targetEquipmentStatus = "AVAILABLE";
				targetLocation = "Central Depot Yard";
			}

			Map<String, String> statusBody = Map.of(
				"status", targetEquipmentStatus,
				"location", targetLocation
			);

			client.put()
					.uri(statusUpdateUrl)
					.contentType(MediaType.APPLICATION_JSON)
					.body(statusBody)
					.retrieve()
					.toBodilessEntity();

		} catch (Exception ex) {
			System.err.println("[DispatchService] Warning: Could not sync equipment status: " + ex.getMessage());
		}

		response.put("code", 200);
		response.put("status", "Dispatch Status Updated Successfully");
		response.put("dispatchTrackingNumber", dispatch.getDispatchTrackingNumber());
		response.put("currentStatus", dispatch.getDispatchStatus());
		response.put("data", dispatch);

		return response;
	}

	public Object getTrackingDetails(String trackingNumber) {
		Map<String, Object> response = new HashMap<>();
		DispatchRecord record = DR.findByDispatchTrackingNumber(trackingNumber);
		if (record == null) {
			response.put("code", 404);
			response.put("message", "Tracking record not found for: " + trackingNumber);
			return response;
		}
		response.put("code", 200);
		response.put("data", record);
		return response;
	}

	public Object getDispatchesByBooking(String bookingReference) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<DispatchRecord> list = DR.findByBookingReference(bookingReference);
			response.put("code", 200);
			response.put("bookingReference", bookingReference);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}

	public Object getAllDispatches() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<DispatchRecord> list = DR.findAll();
			response.put("code", 200);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}
}
