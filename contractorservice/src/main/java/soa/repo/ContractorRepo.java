package soa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import soa.model.Contractor;

@Repository
public interface ContractorRepo extends JpaRepository<Contractor, Long> {

	Contractor findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByLicenseNumber(String licenseNumber);
}
