package edu.hust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.Blacklist;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Integer> {

}
