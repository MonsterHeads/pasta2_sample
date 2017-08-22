package huck.pasta2.sample.db;

import java.sql.SQLException;

import huck.common.db.DBHelper;
import huck.common.db.DBResultSet;

public class SampleDAO {
	public static void insertUser(DBHelper db, String userId) throws SQLException {
		db.executeUpdate("INSERT INTO user (user_id) VALUES (?)", userId);
	}
	
	public static void insertUserInfo(DBHelper db, String userId, String userInfo) throws SQLException {
		db.executeUpdate("INSERT INTO user_info (user_id, user_info) VALUES (?,?)", userId, userInfo);
	}

	public static User selectUser(DBHelper db, String userId) throws SQLException {
		String sql = "SELECT a.user_id, b.user_info FROM user a JOIN user_info b ON a.user_id=b.user_id WHERE a.user_id=?";
		DBResultSet rs = db.executeQuery(sql, userId);
		if( rs.next() ) {
			String dbUserId = rs.getString("user_id");
			String dbUserInfo = rs.getString("user_info");
			User result = new User();
			result.setUserId(dbUserId);
			result.setUserInfo(dbUserInfo);
			return result;
		} else {
			return null;
		}
	}
}
