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

public class Calculate_D1 {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<Integer> final_point = new ArrayList<Integer>();
	boolean print_message = true;
	
	public Calculate_D1(List<String> selected_years) {
		// Connect to a database. Single connection can work the same as multiple connections (code for multiple connections is deleted)
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			// I have to change type of the [CURR_INCIDENT_AREA] from nvarchar to float in 2015 and 2016 Sit Report table, otherwise it will fail if select 2 years for ranking (1 of them is 2015 or 2016)
			// This is strange because when I select all 5 years it does not fail. WTF!
			String sql_2015 = 
					"""
					SELECT 2015 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER], [CURR_INCIDENT_AREA], [SINGLE_COMPLEX_FLAG], [COMPLEXITY_LEVEL_IDENTIFIER], 
					CASE WHEN CAST([CURR_INCIDENT_AREA] AS FLOAT)>=25000 THEN 5
					WHEN CAST([CURR_INCIDENT_AREA] AS FLOAT)>=15000 AND CAST([CURR_INCIDENT_AREA] AS FLOAT)<25000 THEN 4
					WHEN CAST([CURR_INCIDENT_AREA] AS FLOAT)>=5000 AND CAST([CURR_INCIDENT_AREA] AS FLOAT)<15000 THEN 3
					WHEN CAST([CURR_INCIDENT_AREA] AS FLOAT)>=2500 AND CAST([CURR_INCIDENT_AREA] AS FLOAT)<5000 THEN 2
					WHEN CAST([CURR_INCIDENT_AREA] AS FLOAT)>=1 AND CAST([CURR_INCIDENT_AREA] AS FLOAT)<2500 THEN 1
					ELSE 0 END AS D1_Points
					FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					""";
			String[] sql = new String[selected_years.size()];
			for (int i = 0; i < selected_years.size(); i++) {
				sql[i] = sql_2015.replaceAll("2015", selected_years.get(i));
			}
			String final_sql = String.join(" UNION ", sql) + " ORDER BY INC_IDENTIFIER, INC209R_IDENTIFIER";
			resultSet = statement.executeQuery(final_sql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				INC.add(resultSet.getString(3));
				final_point.add(resultSet.getInt(7));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
