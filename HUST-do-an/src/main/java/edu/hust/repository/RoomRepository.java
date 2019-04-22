package edu.hust.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer>{

	List<Room> findAll();

	boolean findByAddress(String address);

	Optional<Room> findByIdAndMacAddress(int roomID, String macAddress);
	
}
