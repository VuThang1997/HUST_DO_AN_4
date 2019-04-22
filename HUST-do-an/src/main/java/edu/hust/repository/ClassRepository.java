package edu.hust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.Class;

@Repository
public interface ClassRepository extends JpaRepository<Class, Integer>, CustomClassRepository{

}
