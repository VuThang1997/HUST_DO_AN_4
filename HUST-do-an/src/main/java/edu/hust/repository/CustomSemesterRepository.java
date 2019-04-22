package edu.hust.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Controller;

@Controller
public interface CustomSemesterRepository {

	@Query("SELECT se.semesterID FROM Semester se WHERE ?1 BETWEEN se.beginDate AND se.endDate")
	Optional<Integer> getSemesterIDByTime(Date date);
}
