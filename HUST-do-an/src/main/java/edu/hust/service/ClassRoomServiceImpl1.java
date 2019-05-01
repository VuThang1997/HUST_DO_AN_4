
package edu.hust.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.ClassRoom;
import edu.hust.repository.ClassRoomRepository;

@Service
@Qualifier("ClassRoomServiceImpl1")
public class ClassRoomServiceImpl1 implements ClassRoomService {

	private ClassRoomRepository classRoomRepository;
	public ClassRoomServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub }
	}

	@Autowired
	public ClassRoomServiceImpl1(ClassRoomRepository classRoomRepository) {
		super();
		this.classRoomRepository = classRoomRepository;
	}

	@Override
	public boolean addNewClassRoom(ClassRoom classRoom) {
		if (classRoom.getId() > 0) {
			classRoom.setId(-1);
		}

		this.classRoomRepository.save(classRoom);
		return true;
	}

	@Override
	public List<ClassRoom> getListClassRoom(int classID, int roomID) {
		return this.classRoomRepository.findByClassIDAndRoomID(classID, roomID);
	}

	@Override
	public ClassRoom getInfoClassRoom(int classID, int roomID, int weekday, LocalTime checkTime) {
		Optional<ClassRoom> classRoom = this.classRoomRepository.findByClassIDAndRoomIDAndWeekday(classID, roomID,
				weekday, checkTime);
		
		return classRoom.isPresent() ? classRoom.get() : null;
	}

	@Override
	public List<ClassRoom> checkClassAvailable(int classID, int weekday, LocalTime beginAt, LocalTime finishAt) {
		List<ClassRoom> listClass = this.classRoomRepository.findClassesByIdAndWeekdayAndDuration(classID, weekday, beginAt, finishAt);
		if (listClass == null || listClass.isEmpty()) {
			return null;
		}
		
		return listClass;
	}

	@Override
	public List<ClassRoom> checkRoomAvailable(int roomID, int weekday, LocalTime beginAt, LocalTime finishAt) {
		List<ClassRoom> listRoom = this.classRoomRepository.findRoomByIdAndWeekdayAndDuration(roomID, weekday, beginAt, finishAt);
		if (listRoom == null || listRoom.isEmpty()) {
			return null;
		}
		return listRoom;
	}

	@Override
	public ClassRoom findClassRoomByID(int id) {
		Optional<ClassRoom> classRoom = this.classRoomRepository.findById(id);
		if (classRoom.isPresent()) {
			return classRoom.get();
		}
		return null;
	}

	@Override
	public void updateClassRoomInfo(ClassRoom classRoom) {
		this.classRoomRepository.save(classRoom);
		return;
	}

	@Override
	public boolean deleteClassRoom(ClassRoom classRoom) {
		try {
			this.classRoomRepository.deleteById(classRoom.getId());
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	public List<ClassRoom> findClassRoomByWeekday(int currentDay) {
		List<ClassRoom> listClassRoom = this.classRoomRepository.findByWeekday(currentDay);
		if (listClassRoom == null || listClassRoom.isEmpty()) {
			return null;
		}
		return listClassRoom;
	}

}
