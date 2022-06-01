/* Note: 
 * https://blog.sqlauthority.com/2019/03/01/sql-server-sql-server-configuration-manager-missing-from-start-menu/
 * https://docs.microsoft.com/en-us/answers/questions/499956/jdbc-connection-issue.html
 * https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-windows-firewall-for-database-engine-access?view=sql-server-ver16
 * https://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed
 * Important: use JDBC driver 9.4 JRE 11 because it is compatible with JDK15 
 * Important: add sqljdbc_xa.dll to the Native Location of the jar in the Build Path
 */
package root;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLserver {

	public SQLserver() {
		// Connect to your database.
		String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=DATALINK2;integratedSecurity=true";
        ResultSet resultSet = null;
		try (Connection connection = DriverManager.getConnection(connectionUrl); Statement statement = connection.createStatement();) {
			// Create and execute a SELECT SQL statement.
			String selectSql = "SELECT * FROM [2015_SIT_IMSR_Intermediate2]";
			resultSet = statement.executeQuery(selectSql);
			// Print results from select statement
			while (resultSet.next()) {
				System.out.println(resultSet.getString(2) + " " + resultSet.getString(3));
			}
		}
        catch (SQLException e) {
            e.printStackTrace();
        }
	}
}
