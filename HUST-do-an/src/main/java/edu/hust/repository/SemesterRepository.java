package edu.hust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.Semester;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer>, CustomSemesterRepository{

}
