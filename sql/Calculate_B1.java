/* Note: 
 * https://blog.sqlauthority.com/2019/03/01/sql-server-sql-server-configuration-manager-missing-from-start-menu/
 * https://docs.microsoft.com/en-us/answers/questions/499956/jdbc-connection-issue.html
 * https://docs.microsoft.com/en-us/sql/database-engine/configure-windows/configure-a-windows-firewall-for-database-engine-access?view=sql-server-ver16
 * https://stackoverflow.com/questions/18841744/jdbc-connection-failed-error-tcp-ip-connection-to-host-failed
 * Important: use JDBC driver 9.4 JRE 11 because it is compatible with JDK15 
 * Important: add sqljdbc_xa.dll to the Native Location of the jar in the Build Path
 */
package sql;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;
import root.IMSRmain;

public class Calculate_B1 {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<Integer> damaged = new ArrayList<Integer>();
	List<Integer> destroyed = new ArrayList<Integer>();
	List<Integer> threatened = new ArrayList<Integer>();
	List<Integer> total = new ArrayList<Integer>();
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
				damaged.add(resultSet.getInt(4));
				destroyed.add(resultSet.getInt(5));
				threatened.add(resultSet.getInt(6));
				total.add(resultSet.getInt(7));
				final_point.add(resultSet.getInt(8));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void show_B1_scroll() {
		new B1_Scroll();
	}
	
	class B1_Scroll extends JScrollPane {
		public B1_Scroll() {		
			String[] header = new String[] { "RECORD", "YEAR", "INC", "INC209R", "DAMAGED", "DESTROYED", "THREATENED", "TOTAL", "Final_Point" };
			TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
			textarea.append(String.join("\t", header)  + "\n");
			int number_of_records = year.size();
			for (int i = 0; i < number_of_records; i++) {
				textarea.append(String.valueOf(i+1)
						+ "\t" + year.get(i) 
						+ "\t" + INC.get(i) 
						+ "\t" + INC209R.get(i) 
						+ "\t" + damaged.get(i) 
						+ "\t" + destroyed.get(i)
						+ "\t" + threatened.get(i) 
						+ "\t" + total.get(i) 
						+ "\t" + final_point.get(i) 
						+ "\n");
			}
			textarea.setSelectionStart(0);	// scroll to top
			textarea.setSelectionEnd(0);
			textarea.setEditable(false);
			
			TitleScrollPane explore_scrollpane = new TitleScrollPane("", "CENTER", textarea);
			addHierarchyListener(new HierarchyListener() {	//	These codes make the license_scrollpane resizable --> the Big ScrollPane resizable --> JOptionPane resizable
			    public void hierarchyChanged(HierarchyEvent e) {
			        Window window = SwingUtilities.getWindowAncestor(explore_scrollpane);
			        if (window instanceof Dialog) {
			            Dialog dialog = (Dialog)window;
			            if (!dialog.isResizable()) {
			                dialog.setResizable(true);
			                dialog.setPreferredSize(new Dimension((int) (IMSRmain.get_main().getWidth() / 1.1), (int) (IMSRmain.get_main().getHeight() / 1.21)));
			            }
			        }
			    }
			});
			
			// Add the Panel to this Big ScrollPane
			setBorder(BorderFactory.createEmptyBorder());
			setViewportView(explore_scrollpane);
			
			// Add everything to a popup panel
			String ExitOption[] = {"EXIT" };
			int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), this, "California Priority Points",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
		}
	}
}
