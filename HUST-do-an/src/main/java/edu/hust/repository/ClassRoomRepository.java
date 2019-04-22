
package edu.hust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.ClassRoom;

@Repository
	public interface ClassRoomRepository extends JpaRepository<ClassRoom, Integer>, CustomClassRoomRepository {

}
