
package edu.hust.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.hust.model.ClassRoom;

@Repository
public interface ClassRoomRepository extends JpaRepository<ClassRoom, Integer>, CustomClassRoomRepository {

	@Query("SELECT cr FROM ClassRoom cr WHERE cr.weekday = ?2 AND cr.classInstance.id = ?1 AND ((?3 BETWEEN cr.beginAt AND cr.finishAt)"
			+ " OR (?4 BETWEEN cr.beginAt AND cr.finishAt) OR (?3 < cr.beginAt AND cr.finishAt > ?4))")
	List<ClassRoom> findClassesByIdAndWeekdayAndDuration(int classID, int weekday, LocalTime beginAt, LocalTime finishAt);

	@Query("SELECT cr FROM ClassRoom cr WHERE cr.weekday = ?2 AND cr.room.id = ?1 AND ((?3 BETWEEN cr.beginAt AND cr.finishAt)"
			+ " OR (?4 BETWEEN cr.beginAt AND cr.finishAt) OR (?3 < cr.beginAt AND cr.finishAt > ?4))")
	List<ClassRoom> findRoomByIdAndWeekdayAndDuration(int roomID, int weekday, LocalTime beginAt, LocalTime finishAt);

	List<ClassRoom> findByWeekday(int weekday);

	@Query("SELECT cr FROM ClassRoom cr WHERE cr.classInstance.id = ?1")
	List<ClassRoom> findByClassID(int classID);

}
