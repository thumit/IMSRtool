package root;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import convenience_classes.GridBagLayoutHandle;
import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;

public class OptionPane_Explore extends JOptionPane {
	public OptionPane_Explore(File[] file) {
		int id = 0;
		boolean exit_exploration = false;
		do {
			Explore_Panel explore_panel = new Explore_Panel(file[id]);
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(explore_panel);

			String ExitOption[] = { "NEXT", "AGGREGATE", "EXIT" };
			int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), scroll, "EXPLORE",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
			if (response == 0 && id < file.length) { // Next
				id = id + 1;
			} else if (response == 1) {
				exit_exploration = true;
				new Aggregate_Scroll(file);
			} else {
				exit_exploration = true;
			}
		} while (exit_exploration == false && id < file.length);
	}
}

class Aggregate_Scroll extends JScrollPane {
	public Aggregate_Scroll(File[] file) {		
		String[] header1 = new String[] { "date", "national_prepareness_level", "initial_attack_activity",
				"initial_attack_new_fires", "new_large_incidents", "large_fires_contained",
				"uncontained_large_fires", "area_command_teams_committed", "NIMOs_committed", "type_1_IMTs_committed",
				"type_2_IMTs_committed" };
		String[] header2 = new String[] { "date", "gacc", "gacc_priority", "fire_priority", "incident_name", "unit", "size_acres",
				"size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw",
				"resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own" };
		
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
		textarea.append(String.join("\t", header1)  + "\n");
		for (File f : file) {
			ISMR_Process ismr = new ISMR_Process(f);
			textarea.append(String.join("\t", ismr.national_fire_activity)  + "\n");
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header2)  + "\n");
		for (File f : file) {
			ISMR_Process ismr = new ISMR_Process(f);
			for (String fire : ismr.all_fires) {
				textarea.append(fire + "\n");
			}
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
		int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), this, "EXPLORE",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
	}
}

class Explore_Panel extends JPanel{
	public Explore_Panel(File file) {	
		ScrollPane_TrimFile trim = new ScrollPane_TrimFile(file);
		TitledBorder border = new TitledBorder(file.getName().toString() + " - LINES HAVE LEADING AND ENDING SPACES TRIMMED");
		border.setTitleJustification(TitledBorder.CENTER);
		trim.setBorder(border);
		
		ScrollPane_FinalFile result = new ScrollPane_FinalFile(file);
		border = new TitledBorder(file.getName().toString() + " - CUSTOMIZED RESULT");
		border.setTitleJustification(TitledBorder.CENTER);
		result.setBorder(border);		
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		add(trim, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		add(result, GridBagLayoutHandle.get_c(c, "BOTH", 
				1, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
	}
}

class ScrollPane_OriginalFile extends JScrollPane {
	public ScrollPane_OriginalFile(File file) {	
		// Print to text area------------------	
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);
		textarea.setEditable(false);
		BufferedReader buff = null;
		try {
			buff = new BufferedReader(new FileReader(file));
			String str;
			while ((str = buff.readLine()) != null) {
				textarea.append("\n" + str);
			}
			textarea.setCaretPosition(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		} finally {
			try {
				buff.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
			}
		}
		
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
	}
}

class ScrollPane_TrimFile extends JScrollPane {
	public ScrollPane_TrimFile(File file) {	
		// Print to text area--------------------	
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);
		textarea.setEditable(false);

		try {
//			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);		// Not sure why this UTF_8 fail
			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());
			for (String line : lines_list) {
				textarea.append(line.replaceAll("\\s{2,}", " ").trim() + "\n");		// 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
			}
			textarea.setSelectionStart(0);	// scroll to top
			textarea.setSelectionEnd(0);
			lines_list = null; // free memory
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		textarea.setCaretPosition(0);
		
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
	}
}

class ScrollPane_FinalFile extends JScrollPane {
	public ScrollPane_FinalFile(File file) {
		String[] header1 = new String[] { "date", "national_prepareness_level", "initial_attack_activity",
				"initial_attack_new_fires", "new_large_incidents", "large_fires_contained",
				"uncontained_large_fires", "area_command_teams_committed", "NIMOs_committed", "type_1_IMTs_committed",
				"type_2_IMTs_committed" };
		String[] header2 = new String[] { "date", "gacc", "gacc_priority", "fire_priority", "incident_name", "unit", "size_acres",
				"size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw",
				"resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own" };
		
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
		ISMR_Process ismr = new ISMR_Process(file);
		textarea.append(ismr.date + "\n");
		textarea.append(ismr.national_prepareness_level + "\n");
		textarea.append(ismr.initial_attack_activity + "\n");
		textarea.append(ismr.initial_attack_new_fires + "\n");
		textarea.append(ismr.new_large_incidents + "\n");
		textarea.append(ismr.large_fires_contained + "\n");
		textarea.append(ismr.uncontained_large_fires + "\n");
		textarea.append(ismr.area_command_teams_committed + "\n");
		textarea.append(ismr.NIMOs_committed + "\n");
		textarea.append(ismr.type_1_IMTs_committed + "\n");
		textarea.append(ismr.type_2_IMTs_committed + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header2)  + "\n");
		for (String fire : ismr.all_fires) {
			textarea.append(fire + "\n");
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
	}
}
