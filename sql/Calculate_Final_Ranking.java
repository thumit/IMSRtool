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

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;
import root.IMSRmain;

public class Calculate_Final_Ranking {
	Calculate_A2 A2;
	Calculate_B1 B1;
	int number_of_records;

	public Calculate_Final_Ranking() {
		A2 = new Calculate_A2();
		B1 = new Calculate_B1();
		number_of_records = A2.year.size();
		new Ranking_Points_Scroll();
	}
	
	class Ranking_Points_Scroll extends JScrollPane {
		public Ranking_Points_Scroll() {		
			String[] header = new String[] { "RECORD", "YEAR", "INC", "INC209R", "A2", "B1"};
			TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
			textarea.append(String.join("\t", header)  + "\n");
			for (int i = 0; i < number_of_records; i++) {
				textarea.append(String.valueOf(i+1)
						+ "\t" + A2.year.get(i) 
						+ "\t" + A2.INC.get(i) 
						+ "\t" + A2.INC209R.get(i) 
						+ "\t" + A2.final_point.get(i)
						+ "\t" + B1.final_point.get(i)
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
