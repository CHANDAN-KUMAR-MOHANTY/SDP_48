package soa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rental_bookings")
public class RentalBooking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "booking_reference", unique = true, nullable = false)
	private String bookingReference;

	@Column(name = "contractor_username", nullable = false)
	private String contractorUsername;

	@Column(name = "equipment_id", nullable = false)
	private Long equipmentId;

	@Column(name = "equipment_name")
	private String equipmentName;

	@Column(name = "start_date", nullable = false)
	private String startDate; // format: YYYY-MM-DD

	@Column(name = "end_date", nullable = false)
	private String endDate; // format: YYYY-MM-DD

	@Column(name = "total_days", nullable = false)
	private Long totalDays;

	@Column(name = "daily_rate")
	private Double dailyRate;

	@Column(name = "total_cost", nullable = false)
	private Double totalCost;

	@Column(name = "delivery_site_address", nullable = false)
	private String deliverySiteAddress;

	@Column(name = "booking_status", nullable = false)
	private String bookingStatus; // CONFIRMED, DISPATCHED, COMPLETED, CANCELLED

	@Column(name = "created_at")
	private String createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBookingReference() {
		return bookingReference;
	}

	public void setBookingReference(String bookingReference) {
		this.bookingReference = bookingReference;
	}

	public String getContractorUsername() {
		return contractorUsername;
	}

	public void setContractorUsername(String contractorUsername) {
		this.contractorUsername = contractorUsername;
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

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Long getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(Long totalDays) {
		this.totalDays = totalDays;
	}

	public Double getDailyRate() {
		return dailyRate;
	}

	public void setDailyRate(Double dailyRate) {
		this.dailyRate = dailyRate;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	public String getDeliverySiteAddress() {
		return deliverySiteAddress;
	}

	public void setDeliverySiteAddress(String deliverySiteAddress) {
		this.deliverySiteAddress = deliverySiteAddress;
	}

	public String getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
}
