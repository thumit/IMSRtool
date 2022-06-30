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

public class Calculate_B1 {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<Integer> final_point = new ArrayList<Integer>();
	boolean print_message = true;
	
	public Calculate_B1() {
		// Connect to a database. Single connection can work the same as multiple connections (code for multiple connections is deleted)
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			String selectSql = 
					"""
					SELECT 2015 AS [YEAR], tablea.INC209R_IDENTIFIER, tablea.INC_IDENTIFIER, TOTAL_STR_DAMAGED, TOTAL_STR_DESTROYED, TOTAL_STR_THREATENED, TOTAL,
					CASE WHEN TOTAL>=200 THEN 5
					WHEN TOTAL>=100 AND TOTAL<200 THEN 4
					WHEN TOTAL>=25 AND TOTAL<100 THEN 3
					WHEN TOTAL>=5 AND TOTAL<25 THEN 2
					WHEN TOTAL>=1 AND TOTAL<5 THEN 1
					ELSE 0 END AS B1_Points
					FROM
					
					(SELECT INC209R_IDENTIFIER,INC_IDENTIFIER FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]) tablea
					LEFT JOIN		
					(SELECT 
					INC209R_IDENTIFIER,
					SUM(CAST(QTY_DAMAGED AS INT)) AS TOTAL_STR_DAMAGED,
					SUM(CAST(QTY_DESTROYED AS INT)) AS TOTAL_STR_DESTROYED,
					SUM(CAST([QTY_THREATENED_72] AS INT)) AS TOTAL_STR_THREATENED,
					SUM(CAST(QTY_DAMAGED AS INT) + CAST(QTY_DESTROYED AS INT) + CAST([QTY_THREATENED_72] AS INT)) AS TOTAL
					FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_AFFECTED_STRUCTS]
					LEFT JOIN [SIT2015].[dbo].[SIT209_HISTORY_SIT209_LOOKUP_CODES]
					ON SST_IDENTIFIER = LUCODES_IDENTIFIER
					GROUP BY INC209R_IDENTIFIER) tableb				
					ON tablea.INC209R_IDENTIFIER = tableb.INC209R_IDENTIFIER
					

					UNION


					SELECT 2016 AS [YEAR], tablea.INC209R_IDENTIFIER, tablea.INC_IDENTIFIER, TOTAL_STR_DAMAGED, TOTAL_STR_DESTROYED, TOTAL_STR_THREATENED, TOTAL,
					CASE WHEN TOTAL>=200 THEN 5
					WHEN TOTAL>=100 AND TOTAL<200 THEN 4
					WHEN TOTAL>=25 AND TOTAL<100 THEN 3
					WHEN TOTAL>=5 AND TOTAL<25 THEN 2
					WHEN TOTAL>=1 AND TOTAL<5 THEN 1
					ELSE 0 END AS B1_Points
					FROM
					
					(SELECT INC209R_IDENTIFIER,INC_IDENTIFIER FROM [SIT2016].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]) tablea
					LEFT JOIN	
					(SELECT 
					INC209R_IDENTIFIER,
					SUM(CAST(QTY_DAMAGED AS INT)) AS TOTAL_STR_DAMAGED,
					SUM(CAST(QTY_DESTROYED AS INT)) AS TOTAL_STR_DESTROYED,
					SUM(CAST([QTY_THREATENED_72] AS INT)) AS TOTAL_STR_THREATENED,
					SUM(CAST(QTY_DAMAGED AS INT) + CAST(QTY_DESTROYED AS INT) + CAST([QTY_THREATENED_72] AS INT)) AS TOTAL
					FROM [SIT2016].[dbo].[SIT209_HISTORY_INCIDENT_209_AFFECTED_STRUCTS]
					LEFT JOIN [SIT2016].[dbo].[SIT209_LOOKUP_CODES]
					ON SST_IDENTIFIER = LUCODES_IDENTIFIER
					GROUP BY INC209R_IDENTIFIER) tableb			
					ON tablea.INC209R_IDENTIFIER = tableb.INC209R_IDENTIFIER

				
					ORDER BY INC_IDENTIFIER, INC209R_IDENTIFIER
					""";
			resultSet = statement.executeQuery(selectSql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				INC.add(resultSet.getString(3));
				final_point.add(resultSet.getInt(8));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
