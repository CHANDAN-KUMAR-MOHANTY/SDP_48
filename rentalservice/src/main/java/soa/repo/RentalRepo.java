package soa.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import soa.model.RentalBooking;

@Repository
public interface RentalRepo extends JpaRepository<RentalBooking, Long> {

	List<RentalBooking> findByContractorUsername(String contractorUsername);

	RentalBooking findByBookingReference(String bookingReference);

	List<RentalBooking> findByBookingStatus(String bookingStatus);
}
