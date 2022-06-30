/* Note: 
 * https://blog.sqlauthority.com/2019/03/01/sql-server-sql-server-configuration-manager-missing-from-start-menu/
 * https://docs.microsoft.com/en-us/answers/questions/499956/jdbc-connection-issue.html
 * https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-windows-firewall-for-database-engine-access?view=sql-server-ver16
 * https://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed
 * Important: use JDBC driver 9.4 JRE 11 because it is compatible with JDK15 
 * Important: add sqljdbc_xa.dll to the Native Location of the jar in the Build Path
 */
package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Calculate_D2 {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<Integer> final_point = new ArrayList<Integer>();
	boolean print_message = true;
	
	public Calculate_D2() {
		// Connect to a database. Single connection can work the same as multiple connections (code for multiple connections is deleted)
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			String selectSql = 
					"""
					SELECT [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER], [DISCOVERY_DATE], [REPORT_FROM_DATE], [REPORT_TO_DATE], [ANTICIPATED_COMPLETION_DATE],
					HOURS_TO_CONTAIN, DAYS_TO_CONTAIN, 
					CASE WHEN HOURS_TO_CONTAIN>=0 AND HOURS_TO_CONTAIN<72 THEN 5
					WHEN HOURS_TO_CONTAIN>=72 AND DAYS_TO_CONTAIN<8 THEN 4
					WHEN DAYS_TO_CONTAIN>=8 AND DAYS_TO_CONTAIN<15 THEN 3
					WHEN DAYS_TO_CONTAIN>=15 AND DAYS_TO_CONTAIN<22 THEN 2
					ELSE 1 END AS D2_Points
					FROM
		
					(SELECT 2015 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER], [DISCOVERY_DATE], [REPORT_FROM_DATE], [REPORT_TO_DATE], [ANTICIPATED_COMPLETION_DATE],
					DATEDIFF(HOUR, [REPORT_FROM_DATE], [ANTICIPATED_COMPLETION_DATE]) AS HOURS_TO_CONTAIN,
					DATEDIFF(DAY, [REPORT_FROM_DATE], [ANTICIPATED_COMPLETION_DATE]) AS DAYS_TO_CONTAIN				
					FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					UNION
					SELECT 2016 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER], [DISCOVERY_DATE], [REPORT_FROM_DATE], [REPORT_TO_DATE], [ANTICIPATED_COMPLETION_DATE],
					DATEDIFF(HOUR, [REPORT_FROM_DATE], [ANTICIPATED_COMPLETION_DATE]) AS HOURS_TO_CONTAIN,
					DATEDIFF(DAY, [REPORT_FROM_DATE], [ANTICIPATED_COMPLETION_DATE]) AS DAYS_TO_CONTAIN				
					FROM [SIT2016].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]) table1
					
					ORDER BY INC_IDENTIFIER, INC209R_IDENTIFIER
					""";
			resultSet = statement.executeQuery(selectSql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				INC.add(resultSet.getString(3));
				final_point.add(resultSet.getInt(10));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
