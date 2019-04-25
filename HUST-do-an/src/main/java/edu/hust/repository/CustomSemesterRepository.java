package edu.hust.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;

import edu.hust.model.Semester;

@Controller
public interface CustomSemesterRepository {

	@Query("SELECT se FROM Semester se WHERE ?1 BETWEEN se.beginDate AND se.endDate")
	Optional<Semester> getSemesterIDByTime(LocalDate date);
	
	@Query("SELECT se FROM Semester se WHERE ?1 BETWEEN se.beginDate AND se.endDate")
	List<Semester> checkSemesterDuplicate(LocalDate beginDate);
}
