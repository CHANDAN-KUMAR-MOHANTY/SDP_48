package soa.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import soa.model.RentalBooking;
import soa.repo.RentalRepo;

@Service
public class RentalService {

	private final RentalRepo RR;
	private final DiscoveryClient DC;

	@Value("${gateway.url:http://localhost:8000}")
	private String gatewayUrl;

	public RentalService(RentalRepo RR, DiscoveryClient DC) {
		this.RR = RR;
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

	public Object bookRental(RentalBooking booking) {
		Map<String, Object> response = new HashMap<>();

		if (booking.getContractorUsername() == null || booking.getContractorUsername().isBlank() ||
			booking.getEquipmentId() == null ||
			booking.getStartDate() == null || booking.getStartDate().isBlank() ||
			booking.getEndDate() == null || booking.getEndDate().isBlank() ||
			booking.getDeliverySiteAddress() == null || booking.getDeliverySiteAddress().isBlank()) {

			response.put("code", 400);
			response.put("status", "Validation Failed");
			response.put("message", "Missing required fields: contractorUsername, equipmentId, startDate, endDate, deliverySiteAddress");
			return response;
		}

		LocalDate start;
		LocalDate end;
		try {
			start = LocalDate.parse(booking.getStartDate());
			end = LocalDate.parse(booking.getEndDate());
		} catch (Exception e) {
			response.put("code", 400);
			response.put("status", "Invalid Date Format");
			response.put("message", "Dates must be formatted as YYYY-MM-DD (e.g. 2026-10-01)");
			return response;
		}

		if (end.isBefore(start)) {
			response.put("code", 400);
			response.put("status", "Invalid Date Range");
			response.put("message", "End date cannot be prior to start date");
			return response;
		}

		// Workflow step: Duration calculation
		long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
		booking.setTotalDays(totalDays);

		// Workflow step: Inter-service call to verify equipment availability
		RestClient client = RestClient.create();
		String equipmentBase = getServiceBaseUrl("equipmentservice");
		String equipmentUrl = equipmentBase + "/api/equipment/" + booking.getEquipmentId();

		Map<String, Object> eqResponse;
		try {
			eqResponse = client.get()
					.uri(equipmentUrl)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(new ParameterizedTypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			response.put("code", 503);
			response.put("status", "Equipment Service Communication Error");
			response.put("message", "Unable to connect to Equipment Service: " + e.getMessage());
			return response;
		}

		if (eqResponse == null || !eqResponse.containsKey("data")) {
			response.put("code", 404);
			response.put("status", "Not Found");
			response.put("message", "Equipment not found with ID: " + booking.getEquipmentId());
			return response;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> eqData = (Map<String, Object>) eqResponse.get("data");
		String currentStatus = (String) eqData.get("status");
		String equipmentName = (String) eqData.get("equipmentName");
		Number dailyRateNum = (Number) eqData.get("dailyRate");
		double dailyRate = dailyRateNum != null ? dailyRateNum.doubleValue() : 0.0;

		if (!"AVAILABLE".equalsIgnoreCase(currentStatus)) {
			response.put("code", 409);
			response.put("status", "Conflict");
			response.put("message", "Equipment [" + equipmentName + "] is currently " + currentStatus + " and cannot be rented.");
			return response;
		}

		// Workflow step: Pricing calculation
		double totalCost = totalDays * dailyRate;
		booking.setEquipmentName(equipmentName);
		booking.setDailyRate(dailyRate);
		booking.setTotalCost(totalCost);
		booking.setBookingStatus("CONFIRMED");
		booking.setBookingReference("BA-RENT-" + (System.currentTimeMillis() % 1000000));
		booking.setCreatedAt(LocalDate.now().toString());

		try {
			RentalBooking saved = RR.save(booking);

			// Workflow step: Update equipment status to RENTED
			try {
				String statusUpdateUrl = equipmentBase + "/api/equipment/" + booking.getEquipmentId() + "/status";
				Map<String, String> statusBody = Map.of(
					"status", "RENTED",
					"location", booking.getDeliverySiteAddress()
				);
				client.put()
						.uri(statusUpdateUrl)
						.contentType(MediaType.APPLICATION_JSON)
						.body(statusBody)
						.retrieve()
						.toBodilessEntity();
			} catch (Exception ex) {
				System.err.println("[RentalService] Warning: Could not update equipment status: " + ex.getMessage());
			}

			response.put("code", 200);
			response.put("status", "Rental Booking Confirmed Successfully");
			response.put("bookingReference", saved.getBookingReference());
			response.put("equipmentName", saved.getEquipmentName());
			response.put("totalDays", saved.getTotalDays());
			response.put("dailyRate", saved.getDailyRate());
			response.put("totalCost", saved.getTotalCost());
			response.put("data", saved);

		} catch (Exception e) {
			response.put("code", 500);
			response.put("status", "Booking Error");
			response.put("message", e.getMessage());
		}

		return response;
	}

	public Object getAllRentals() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<RentalBooking> list = RR.findAll();
			response.put("code", 200);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}

	public Object getBookingById(Long id) {
		Map<String, Object> response = new HashMap<>();
		Optional<RentalBooking> opt = RR.findById(id);
		if (opt.isEmpty()) {
			response.put("code", 404);
			response.put("message", "Rental booking not found with ID: " + id);
			return response;
		}
		response.put("code", 200);
		response.put("data", opt.get());
		return response;
	}

	public Object getBookingByReference(String reference) {
		Map<String, Object> response = new HashMap<>();
		RentalBooking booking = RR.findByBookingReference(reference);
		if (booking == null) {
			response.put("code", 404);
			response.put("message", "Rental booking not found for reference: " + reference);
			return response;
		}
		response.put("code", 200);
		response.put("data", booking);
		return response;
	}

	public Object getContractorRentals(String username) {
		Map<String, Object> response = new HashMap<>();
		try {
			List<RentalBooking> list = RR.findByContractorUsername(username);
			response.put("code", 200);
			response.put("contractor", username);
			response.put("count", list.size());
			response.put("data", list);
		} catch (Exception e) {
			response.put("code", 500);
			response.put("message", e.getMessage());
		}
		return response;
	}

	public Object cancelRental(Long id) {
		Map<String, Object> response = new HashMap<>();
		Optional<RentalBooking> opt = RR.findById(id);
		if (opt.isEmpty()) {
			response.put("code", 404);
			response.put("message", "Rental booking not found with ID: " + id);
			return response;
		}

		RentalBooking booking = opt.get();
		booking.setBookingStatus("CANCELLED");
		RR.save(booking);

		// Release equipment back to AVAILABLE
		try {
			String equipmentBase = getServiceBaseUrl("equipmentservice");
			String statusUpdateUrl = equipmentBase + "/api/equipment/" + booking.getEquipmentId() + "/status";
			RestClient client = RestClient.create();
			Map<String, String> statusBody = Map.of(
				"status", "AVAILABLE",
				"location", "Central Depot Yard"
			);
			client.put()
					.uri(statusUpdateUrl)
					.contentType(MediaType.APPLICATION_JSON)
					.body(statusBody)
					.retrieve()
					.toBodilessEntity();
		} catch (Exception ex) {
			System.err.println("[RentalService] Warning: Could not reset equipment status: " + ex.getMessage());
		}

		response.put("code", 200);
		response.put("status", "Booking Cancelled and Equipment Released Successfully");
		response.put("bookingReference", booking.getBookingReference());
		return response;
	}
}
