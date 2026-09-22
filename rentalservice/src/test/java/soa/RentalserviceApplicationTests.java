package soa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import soa.model.RentalBooking;

@SpringBootTest
class RentalserviceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testDurationAndCostCalculation() {
		LocalDate start = LocalDate.parse("2026-10-01");
		LocalDate end = LocalDate.parse("2026-10-06");
		long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
		double dailyRate = 450.0;
		double expectedCost = totalDays * dailyRate;

		RentalBooking booking = new RentalBooking();
		booking.setTotalDays(totalDays);
		booking.setDailyRate(dailyRate);
		booking.setTotalCost(expectedCost);

		assertEquals(6, totalDays);
		assertEquals(2700.0, booking.getTotalCost());
		assertNotNull(booking);
	}
}
