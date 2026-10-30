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

import soa.model.RentalBooking;
import soa.repo.RentalRepo;
import soa.service.RentalService;

@ExtendWith(MockitoExtension.class)
public class RentalServiceUnitTest {

	@Mock
	private RentalRepo rentalRepo;

	@Mock
	private DiscoveryClient discoveryClient;

	private RentalService rentalService;

	@BeforeEach
	void setUp() {
		rentalService = new RentalService(rentalRepo, discoveryClient);
	}

	@Test
	@DisplayName("Negative: Rejects booking with missing mandatory fields")
	void testMissingFieldsValidation() {
		RentalBooking booking = new RentalBooking();
		booking.setContractorUsername("john_contractor");

		Object res = rentalService.bookRental(booking);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(400, respMap.get("code"));
		assertEquals("Validation Failed", respMap.get("status"));
	}

	@Test
	@DisplayName("Negative: Rejects invalid date format")
	void testInvalidDateFormat() {
		RentalBooking booking = new RentalBooking();
		booking.setContractorUsername("john_contractor");
		booking.setEquipmentId(1L);
		booking.setStartDate("01-10-2026");
		booking.setEndDate("2026-10-05");
		booking.setDeliverySiteAddress("Highway 44 Junction");

		Object res = rentalService.bookRental(booking);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(400, respMap.get("code"));
		assertEquals("Invalid Date Format", respMap.get("status"));
	}

	@Test
	@DisplayName("Negative: Rejects end date before start date")
	void testInvalidDateRange() {
		RentalBooking booking = new RentalBooking();
		booking.setContractorUsername("john_contractor");
		booking.setEquipmentId(1L);
		booking.setStartDate("2026-10-10");
		booking.setEndDate("2026-10-05");
		booking.setDeliverySiteAddress("Highway 44 Junction");

		Object res = rentalService.bookRental(booking);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(400, respMap.get("code"));
		assertEquals("Invalid Date Range", respMap.get("status"));
	}

	@Test
	@DisplayName("Positive: Successfully cancels booking and releases equipment")
	void testCancelRental() {
		RentalBooking booking = new RentalBooking();
		booking.setId(201L);
		booking.setEquipmentId(10L);
		booking.setBookingReference("BA-RENT-9999");
		booking.setBookingStatus("CONFIRMED");

		when(rentalRepo.findById(201L)).thenReturn(Optional.of(booking));
		when(rentalRepo.save(any(RentalBooking.class))).thenAnswer(inv -> inv.getArgument(0));

		Object res = rentalService.cancelRental(201L);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(200, respMap.get("code"));
		assertEquals("Booking Cancelled and Equipment Released Successfully", respMap.get("status"));
		assertEquals("BA-RENT-9999", respMap.get("bookingReference"));
	}

	@Test
	@DisplayName("Negative: Returns 404 when cancelling non-existent booking")
	void testCancelNotFound() {
		when(rentalRepo.findById(999L)).thenReturn(Optional.empty());

		Object res = rentalService.cancelRental(999L);
		@SuppressWarnings("unchecked")
		Map<String, Object> respMap = (Map<String, Object>) res;

		assertEquals(404, respMap.get("code"));
		assertTrue(respMap.get("message").toString().contains("Rental booking not found"));
	}
}
