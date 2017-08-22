package huck.pasta2.sample.modules;

import java.sql.SQLException;

import huck.common.db.DBConnectionManager;
import huck.common.db.DBHelper;
import huck.pasta2.sample.db.SampleDAO;
import huck.pasta2.sample.db.User;

public class SampleModule {
	private DBConnectionManager dbConnMgr;
	public SampleModule(DBConnectionManager dbConnMgr) {
		this.dbConnMgr = dbConnMgr;
	}
	
	public void addUser(String userId, String userInfo) throws SQLException {
		try( DBHelper db = new DBHelper(dbConnMgr, false) ) {
			SampleDAO.insertUser(db, userId);
			SampleDAO.insertUserInfo(db, userId, userInfo);
			db.commit();
		}
	}
	
	public User getUser(String userId) throws SQLException {
		try( DBHelper db = new DBHelper(dbConnMgr, true) ) {
			return SampleDAO.selectUser(db, userId);
		}
	}
}
