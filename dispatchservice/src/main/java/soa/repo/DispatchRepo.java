package soa.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import soa.model.DispatchRecord;

@Repository
public interface DispatchRepo extends JpaRepository<DispatchRecord, Long> {

	DispatchRecord findByDispatchTrackingNumber(String dispatchTrackingNumber);

	List<DispatchRecord> findByBookingReference(String bookingReference);

	List<DispatchRecord> findByDispatchStatus(String dispatchStatus);

	List<DispatchRecord> findByEquipmentId(Long equipmentId);
}
