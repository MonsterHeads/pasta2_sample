package huck.common.db;

import java.sql.Connection;
import java.sql.SQLException;

public class NullDBConnectionManager extends DBConnectionManager {
	public NullDBConnectionManager() {
		super(null);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return null;
	}
}
