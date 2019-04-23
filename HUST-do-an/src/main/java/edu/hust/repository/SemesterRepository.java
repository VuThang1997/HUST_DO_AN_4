package edu.hust.repository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer>, CustomSemesterRepository{

	Optional<Semester> findBySemesterName(String semesterName);

	@Transactional
	void deleteBySemesterName(String semesterName);

}
