
package edu.hust.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.model.ClassRoom;
import edu.hust.repository.ClassRepository;
import edu.hust.repository.ClassRoomRepository;
import edu.hust.repository.RoomRepository;

@Service

@Qualifier("ClassRoomServiceImpl1")
public class ClassRoomServiceImpl1 implements ClassRoomService {

	private ClassRoomRepository classRoomRepository;
	private ClassRepository classRepository;
	private RoomRepository roomRepository;

	public ClassRoomServiceImpl1() {
		super();
		// TODO Auto-generated constructor stub }
	}

	@Autowired
	public ClassRoomServiceImpl1(ClassRoomRepository classRoomRepository, ClassRepository classRepository,
			RoomRepository roomRepository) {
		super();
		this.classRoomRepository = classRoomRepository;
		this.classRepository = classRepository;
		this.roomRepository = roomRepository;
	}

	@Override
	public boolean addNewClassRoom(ClassRoom classRoom) {
		if (classRoom.getId() > 0) {
			classRoom.setId(-1);
		}

		if (!this.classRepository.existsById(classRoom.getClassInstance().getId())
				|| !this.roomRepository.existsById(classRoom.getRoom().getId())) {
			return false;
		}

		this.classRoomRepository.save(classRoom);
		return true;
	}

	@Override
	public List<ClassRoom> getListClassRoom(int classID, int roomID) {
		return this.classRoomRepository.findByClassIDAndRoomID(classID, roomID);
	}

	@Override
	public boolean updateClassRoomInfo(int roomID, int classID, ClassRoom classRoom) {
		// Optional<ClassRoom> oldInfo =
		// this.classRoomRepository.findByClassIDAndRoomID(classID, roomID);
		return false;

	}

	@Override
	public ClassRoom getInfoClassRoom(int classID, int roomID, int weekday, LocalTime checkTime) {
		Optional<ClassRoom> classRoom = this.classRoomRepository.findByClassIDAndRoomIDAndWeekday(classID, roomID,
				weekday, checkTime);
		
		return classRoom.isPresent() ? classRoom.get() : null;
	}

}
