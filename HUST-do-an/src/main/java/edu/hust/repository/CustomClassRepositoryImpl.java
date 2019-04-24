package edu.hust.repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CustomClassRepositoryImpl implements CustomClassRepository {

	@Autowired
	private EntityManager entityManager;

	public CustomClassRepositoryImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setNullIdentifyString(int id, String eventName) {
		String sql = " CREATE EVENT IF NOT EXISTS " + eventName
				+ "   ON SCHEDULE AT NOW() + INTERVAL 10 MINUTE "
				+ "   DO "
				+ "    UPDATE class "
				+ "    SET IdentifyString = NULL "
				+ "    WHERE class.ID = " + id;
		Query query = entityManager.createNativeQuery(sql);
		query.executeUpdate();
		return;
	}

	@Override
	public void setIsCheckFalse(int classID, String finishTime, String eventDynamicName) {
		System.out.println("\n\n finish Time = " + finishTime);
		String sql = " CREATE EVENT IF NOT EXISTS '" + eventDynamicName + "'"
				+ "   ON SCHEDULE AT '" + finishTime + "' "
				+ "   DO "
				+ "    UPDATE class "
				+ "    SET IdentifyString = NULL "
				+ "    WHERE class.ID = " + classID;
		Query query = entityManager.createNativeQuery(sql);
		query.executeUpdate();
		return;
		
	}

}
