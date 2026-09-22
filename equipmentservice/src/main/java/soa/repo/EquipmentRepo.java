package soa.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import soa.model.Equipment;

@Repository
public interface EquipmentRepo extends JpaRepository<Equipment, Long> {

	List<Equipment> findByCategory(String category);

	List<Equipment> findByStatus(String status);

	List<Equipment> findByCategoryAndStatus(String category, String status);
}
