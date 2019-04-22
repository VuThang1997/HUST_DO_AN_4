package edu.hust.service;

import java.time.LocalTime;
import java.util.List;

import edu.hust.model.ClassRoom;

public interface ClassRoomService {

	boolean addNewClassRoom(ClassRoom classRoom);

	List<ClassRoom> getListClassRoom(int classID, int roomID);
	
	ClassRoom getInfoClassRoom(int classID, int roomID, int weekday, LocalTime checkTime);

	boolean updateClassRoomInfo(int roomID, int classID, ClassRoom classRoom);
	
	

}
