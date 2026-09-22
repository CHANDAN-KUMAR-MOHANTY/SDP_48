package soa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dispatch_records")
public class DispatchRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "dispatch_tracking_number", unique = true, nullable = false)
	private String dispatchTrackingNumber;

	@Column(name = "booking_reference", nullable = false)
	private String bookingReference;

	@Column(name = "equipment_id", nullable = false)
	private Long equipmentId;

	@Column(name = "equipment_name")
	private String equipmentName;

	@Column(name = "site_location", nullable = false)
	private String siteLocation;

	@Column(name = "site_supervisor_name", nullable = false)
	private String siteSupervisorName;

	@Column(name = "supervisor_contact", nullable = false)
	private String supervisorContact;

	@Column(name = "driver_name")
	private String driverName;

	@Column(name = "vehicle_number")
	private String vehicleNumber;

	@Column(name = "dispatch_status", nullable = false)
	private String dispatchStatus; // SCHEDULED, IN_TRANSIT, DELIVERED_ON_SITE, RETURN_IN_TRANSIT, RETURNED

	@Column(name = "dispatch_date_time")
	private String dispatchDateTime;

	@Column(name = "expected_return_date")
	private String expectedReturnDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDispatchTrackingNumber() {
		return dispatchTrackingNumber;
	}

	public void setDispatchTrackingNumber(String dispatchTrackingNumber) {
		this.dispatchTrackingNumber = dispatchTrackingNumber;
	}

	public String getBookingReference() {
		return bookingReference;
	}

	public void setBookingReference(String bookingReference) {
		this.bookingReference = bookingReference;
	}

	public Long getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(Long equipmentId) {
		this.equipmentId = equipmentId;
	}

	public String getEquipmentName() {
		return equipmentName;
	}

	public void setEquipmentName(String equipmentName) {
		this.equipmentName = equipmentName;
	}

	public String getSiteLocation() {
		return siteLocation;
	}

	public void setSiteLocation(String siteLocation) {
		this.siteLocation = siteLocation;
	}

	public String getSiteSupervisorName() {
		return siteSupervisorName;
	}

	public void setSiteSupervisorName(String siteSupervisorName) {
		this.siteSupervisorName = siteSupervisorName;
	}

	public String getSupervisorContact() {
		return supervisorContact;
	}

	public void setSupervisorContact(String supervisorContact) {
		this.supervisorContact = supervisorContact;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getDispatchStatus() {
		return dispatchStatus;
	}

	public void setDispatchStatus(String dispatchStatus) {
		this.dispatchStatus = dispatchStatus;
	}

	public String getDispatchDateTime() {
		return dispatchDateTime;
	}

	public void setDispatchDateTime(String dispatchDateTime) {
		this.dispatchDateTime = dispatchDateTime;
	}

	public String getExpectedReturnDate() {
		return expectedReturnDate;
	}

	public void setExpectedReturnDate(String expectedReturnDate) {
		this.expectedReturnDate = expectedReturnDate;
	}
}
