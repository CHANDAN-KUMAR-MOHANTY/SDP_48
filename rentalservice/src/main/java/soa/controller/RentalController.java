package soa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import soa.model.RentalBooking;
import soa.service.RentalService;

@RestController
@RequestMapping("api")
public class RentalController {

	private final RentalService RS;

	public RentalController(RentalService RS) {
		this.RS = RS;
	}

	@PostMapping("rentals/book")
	public Object bookRental(@RequestBody RentalBooking booking) {
		return RS.bookRental(booking);
	}

	@GetMapping("rentals")
	public Object getAllRentals() {
		return RS.getAllRentals();
	}

	@GetMapping("rentals/{id}")
	public Object getBookingById(@PathVariable Long id) {
		return RS.getBookingById(id);
	}

	@GetMapping("rentals/reference/{reference}")
	public Object getBookingByReference(@PathVariable String reference) {
		return RS.getBookingByReference(reference);
	}

	@GetMapping("rentals/contractor/{username}")
	public Object getContractorRentals(@PathVariable String username) {
		return RS.getContractorRentals(username);
	}

	@PutMapping("rentals/{id}/cancel")
	public Object cancelRental(@PathVariable Long id) {
		return RS.cancelRental(id);
	}
}
