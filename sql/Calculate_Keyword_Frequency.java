package sql;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
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

public class Calculate_Keyword_Frequency {
	List<String> year = new ArrayList<String>();
	List<String> INC209R = new ArrayList<String>();
	List<String> INC = new ArrayList<String>();
	List<String> box33_row_data = new ArrayList<String>();
	int allword_count = 0;
	int keyword_count = 0;
	List<String> kw_list = new ArrayList<String>();
	List<Integer> kw_freq_list = new ArrayList<Integer>();
	String SIT_Field;
	
	public Calculate_Keyword_Frequency(String SIT_Field) {
		this.SIT_Field = SIT_Field;
		// Connect to a database. Single connection can work the same as multiple connections (code for multiple connections is deleted)
		String combine_st = "";
		ResultSet resultSet = null;
		String conn_SIT2015 = "jdbc:sqlserver://localhost:1433;databaseName=SIT2015;integratedSecurity=true";
		try (Connection connection = DriverManager.getConnection(conn_SIT2015);
				Statement statement = connection.createStatement();
				) {
			// Create and execute a SELECT SQL statement.
			String selectSql = 
					"""
					SELECT 2015 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER], [USER_DEFINED_SIT_FIELD] FROM [SIT2015].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
					ORDER BY INC_IDENTIFIER, INC209R_IDENTIFIER
					""";
//					UNION
//					SELECT 2016 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER],[LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2016].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
//					UNION
//					SELECT 2017 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER],[LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2017].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
//					UNION
//					SELECT 2018 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER],[LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2018].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
//					UNION
//					SELECT 2019 AS [YEAR], [INC209R_IDENTIFIER], [INC_IDENTIFIER],[LIFE_SAFETY_HEALTH_STATUS_NARR] FROM [SIT2019].[dbo].[SIT209_HISTORY_INCIDENT_209_REPORTS]
			selectSql = selectSql.replace("[USER_DEFINED_SIT_FIELD]", SIT_Field);
			resultSet = statement.executeQuery(selectSql);
			while (resultSet.next()) {
				year.add(resultSet.getString(1));
				INC209R.add(resultSet.getString(2));
				INC.add(resultSet.getString(3));
				String st = resultSet.getString(4);
				if (st != null) combine_st = combine_st.concat(".").concat(st);		// https://stackoverflow.com/questions/5076740/whats-the-fastest-way-to-concatenate-two-strings-in-java
				box33_row_data.add(st);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Identify keywords and frequency using Apache Lucene: https://stackoverflow.com/questions/17447045/java-library-for-keywords-extraction-from-input-text
		try {
			SQL_Utilities utilities = new SQL_Utilities();
			List<Keyword> kw = utilities.guessFromString(combine_st);
			for (Keyword i : kw) {
				int freq = i.getFrequency();
				if (freq >= 10) {
					kw_list.add(i.getStem());
					kw_freq_list.add(freq);
					keyword_count = keyword_count + freq;
				}
				allword_count = allword_count + freq;
			}
			double ratio = (double) keyword_count / (double) allword_count * 100;
			new Keyword_Scroll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	class Keyword_Scroll extends JScrollPane {
		public Keyword_Scroll() {		
			String[] header = new String[] { "KEYWORD", "FREQUENCY"};
			TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
			textarea.append("Below are keywords identified from the field " + SIT_Field + "\n");
			textarea.append(String.join("\t", header)  + "\n");
			
			int number_of_keywords = kw_list.size();
			for (int i = 0; i < number_of_keywords; i++) {
				textarea.append(String.valueOf(i+1)	+ "\t" + kw_list.get(i) + "\t" + kw_freq_list.get(i) + "\n");
			}
			textarea.append("Selected word occurence: " + "\t" + keyword_count + "\n");
			textarea.append("All word occurence: " + "\t" + allword_count + "\n");
			textarea.append("selection vs all ratio: " + "\t" + (double) keyword_count / (double) allword_count * 100 + " %" + "\n");
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
