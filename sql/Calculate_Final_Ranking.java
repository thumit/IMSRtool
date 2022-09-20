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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;
import root.IMSRmain;

public class Calculate_Final_Ranking {
	Calculate_A1 A1;
	Calculate_A2 A2;
	Calculate_A3 A3;
	Calculate_B1 B1;
	Calculate_B2 B2;
	Calculate_B3 B3;
	Calculate_C1 C1;
	Calculate_C2 C2;
	Calculate_C3 C3;
	Calculate_C4 C4;
	Calculate_D1 D1;
	Calculate_D2 D2;
	List<String> YEAR;
	List<String> INC;
	List<String> INC209R;
	int number_of_records;

	public Calculate_Final_Ranking(List<String> selected_years, List<String> selected_categories) {
		for (int i = 0; i < selected_categories.size(); i++) {
			if (selected_categories.get(i).contains("A1")) { A1 = new Calculate_A1(selected_years); YEAR = A1.year; INC = A1.INC; INC209R = A1.INC209R; }
			if (selected_categories.get(i).contains("A2")) { A2 = new Calculate_A2(selected_years); YEAR = A2.year; INC = A2.INC; INC209R = A2.INC209R; }
			if (selected_categories.get(i).contains("A3")) { A3 = new Calculate_A3(selected_years); YEAR = A3.year; INC = A3.INC; INC209R = A3.INC209R; }
			if (selected_categories.get(i).contains("B1")) { B1 = new Calculate_B1(selected_years); YEAR = B1.year; INC = B1.INC; INC209R = B1.INC209R; }
			if (selected_categories.get(i).contains("B2")) { B2 = new Calculate_B2(selected_years); YEAR = B2.year; INC = B2.INC; INC209R = B2.INC209R; }
			if (selected_categories.get(i).contains("B3")) { B3 = new Calculate_B3(selected_years); YEAR = B3.year; INC = B3.INC; INC209R = B3.INC209R; }
			if (selected_categories.get(i).contains("C1")) { C1 = new Calculate_C1(selected_years); YEAR = C1.year; INC = C1.INC; INC209R = C1.INC209R; }
			if (selected_categories.get(i).contains("C2")) { C2 = new Calculate_C2(selected_years); YEAR = C2.year; INC = C2.INC; INC209R = C2.INC209R; }
			if (selected_categories.get(i).contains("C3")) { C3 = new Calculate_C3(selected_years); YEAR = C3.year; INC = C3.INC; INC209R = C3.INC209R; }
			if (selected_categories.get(i).contains("C4")) { C4 = new Calculate_C4(selected_years); YEAR = C4.year; INC = C4.INC; INC209R = C4.INC209R; }
			if (selected_categories.get(i).contains("D1")) { D1 = new Calculate_D1(selected_years); YEAR = D1.year; INC = D1.INC; INC209R = D1.INC209R; }
			if (selected_categories.get(i).contains("D2")) { D2 = new Calculate_D2(selected_years); YEAR = D2.year; INC = D2.INC; INC209R = D2.INC209R; }
		}
		number_of_records = YEAR.size();
		new Ranking_Points_Scroll(selected_categories);
	}
	
	class Ranking_Points_Scroll extends JScrollPane {
		public Ranking_Points_Scroll(List<String> selected_categories) {
			List<String> header = new ArrayList<String>();
			header.add("RECORD");
			header.add("YEAR");
			header.add("INC");
			header.add("INC209R");
			for (int i = 0; i < selected_categories.size(); i++) {
				header.add(selected_categories.get(i));
			}
			header.add("TOTAL");
			
			TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
			textarea.append(String.join("\t", header)  + "\n");
			for (int i = 0; i < number_of_records; i++) {
				textarea.append(String.valueOf(i + 1)
						+ "\t" + YEAR.get(i) 
						+ "\t" + new BigDecimal(INC.get(i)).intValue()			// This change is because INC and INC209R printed out as double or as scientific number in its original String value
						+ "\t" + new BigDecimal(INC209R.get(i)).intValue());	// This change is because INC and INC209R printed out as double or as scientific number in its original String value
				int total_points = 0;
				for (int j = 0; j < selected_categories.size(); j++) {
					if (selected_categories.get(j).contains("A1")) { textarea.append("\t" + A1.final_point.get(i)); total_points = total_points + A1.final_point.get(i); }
					if (selected_categories.get(j).contains("A2")) { textarea.append("\t" + A2.final_point.get(i)); total_points = total_points + A2.final_point.get(i); }
					if (selected_categories.get(j).contains("A3")) { textarea.append("\t" + A3.final_point.get(i)); total_points = total_points + A3.final_point.get(i); }
					if (selected_categories.get(j).contains("B1")) { textarea.append("\t" + B1.final_point.get(i)); total_points = total_points + B1.final_point.get(i); }
					if (selected_categories.get(j).contains("B2")) { textarea.append("\t" + B2.final_point.get(i)); total_points = total_points + B2.final_point.get(i); }
					if (selected_categories.get(j).contains("B3")) { textarea.append("\t" + B3.final_point.get(i)); total_points = total_points + B3.final_point.get(i); }
					if (selected_categories.get(j).contains("C1")) { textarea.append("\t" + C1.final_point.get(i)); total_points = total_points + C1.final_point.get(i); }
					if (selected_categories.get(j).contains("C2")) { textarea.append("\t" + C2.final_point.get(i)); total_points = total_points + C2.final_point.get(i); }
					if (selected_categories.get(j).contains("C3")) { textarea.append("\t" + C3.final_point.get(i)); total_points = total_points + C3.final_point.get(i); }
					if (selected_categories.get(j).contains("C4")) { textarea.append("\t" + C4.final_point.get(i)); total_points = total_points + C4.final_point.get(i); }
					if (selected_categories.get(j).contains("D1")) { textarea.append("\t" + D1.final_point.get(i)); total_points = total_points + D1.final_point.get(i); }
					if (selected_categories.get(j).contains("D2")) { textarea.append("\t" + D2.final_point.get(i)); total_points = total_points + D2.final_point.get(i); }
				}
				textarea.append("\t" + total_points + "\n");
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
			int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), this, "Wildfire ranking points based on California prioritization rules",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
		}
	}
}
