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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import convenience_classes.GridBagLayoutHandle;
import convenience_classes.SubstringBetween;
import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;

public class OptionPane_Explore extends JOptionPane {
	public OptionPane_Explore(File file) {
		for (JInternalFrame i : IMSRmain.get_DesktopPane().getAllFrames()) {
			i.setVisible(false);
		}
		
		ScrollPane_TrimmFile trim = new ScrollPane_TrimmFile(file);
		TitledBorder border = new TitledBorder("FILE - LINES HAVE LEADING AND ENDING SPACES TRIMMED");
		border.setTitleJustification(TitledBorder.CENTER);
		trim.setBorder(border);
		
		ScrollPane_FinalFile result = new ScrollPane_FinalFile(file);
		border = new TitledBorder("FILE - FINAL RESULT CUSTOMIZATION");
		border.setTitleJustification(TitledBorder.CENTER);
		result.setBorder(border);		
		
		JPanel combine_panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		combine_panel.add(trim, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		combine_panel.add(result, GridBagLayoutHandle.get_c(c, "BOTH", 
				1, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		
		String ExitOption[] = { "NEXT" };
		int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), combine_panel,
				file.getName().toString(), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
		for (JInternalFrame i : IMSRmain.get_DesktopPane().getAllFrames()) {
			i.setVisible(true);
		}
	}
}

class ScrollPane_OriginalFile extends JScrollPane {
	public ScrollPane_OriginalFile(File file) {	
		// Print to text area-----------------------------------------------------------------------------------------		
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
		
		// Add the Panel to this Big ScrollPane------------------------------------------------------------------------------
		setBorder(BorderFactory.createEmptyBorder());
		setViewportView(explore_scrollpane);			
	}
}

class ScrollPane_TrimmFile extends JScrollPane {
	public ScrollPane_TrimmFile(File file) {	
		// Print to text area-----------------------------------------------------------------------------------------		
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);
		textarea.setEditable(false);
		
		
		
		try {
			// All lines to be in array
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
		
		
		
//		BufferedReader buff = null;
//		try {
//			buff = new BufferedReader(new FileReader(file));
//			String str;
//			while ((str = buff.readLine()) != null) {
//				textarea.append("\n" + str);
//			}
//			textarea.setCaretPosition(0);
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.err.println(e.getClass().getName() + ": " + e.getMessage());
//		} finally {
//			try {
//				buff.close();
//			} catch (Exception ex) {
//				ex.printStackTrace();
//				System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
//			}
//		}
		
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
		
		// Add the Panel to this Big ScrollPane------------------------------------------------------------------------------
		setBorder(BorderFactory.createEmptyBorder());
		setViewportView(explore_scrollpane);			
	}
}

class ScrollPane_FinalFile extends JScrollPane {
	String date;
	String national_prepareness_level;
	String initial_attack_activity;
	String initial_attack_activity_number;
	String new_large_incidents;
	String large_fires_contained;
	String uncontained_large_fires;
	String area_command_teams_committed;
	String NIMOs_committed;
	String type_1_IMTs_committed;
	String type_2_IMTs_committed;
	
	List<String> AICC = new ArrayList<String>();	// Alaska
	List<String> EACC = new ArrayList<String>();	// Eastern
	List<String> GBCC = new ArrayList<String>();	// Great Basin
	List<String> ONCC = new ArrayList<String>();	// Northern California
	List<String> NRCC = new ArrayList<String>();	// Northern Rockies
	List<String> NWCC = new ArrayList<String>();	// Northwest
	List<String> RMCC = new ArrayList<String>();	// Rocky Mountain
	List<String> SACC = new ArrayList<String>();	// Southern Area
	List<String> OSCC = new ArrayList<String>();	// Southern California
	List<String> SWCC = new ArrayList<String>();	// Southwest
	
	String[] lines;
	TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);
	
	public ScrollPane_FinalFile(File file) {	
		// Print to text area-----------------------------------------------------------------------------------------		
		textarea.setEditable(false);
		try {
			// All lines to be in array
//			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);		// Not sure why this UTF_8 fail
			List<String> lines_list = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());
			lines = lines_list.stream().toArray(String[] ::new);
			
			int mergeCount = 0;
			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].replaceAll("\\s{2,}", " ").trim(); // 2 or more spaces will be replaced by one space, then leading and ending spaces will be removed
				if (lines[i].contains("Active Incident Resource Summary")) {
					for (int j = 0; j < i; j++) {
						if (lines[j].contains("Type 2 IMTs")) {
							mergeCount = j + 1;		// Merge up to after this line
						}
					}
				}
			}
			String[] merge_lines = Arrays.copyOfRange(lines, 0, mergeCount + 1);
			String mstr = String.join(" ", merge_lines).toLowerCase().trim();
			textarea.append(mstr + "\n");
			
			date = file.getName().substring(0, 8);
			SubstringBetween sb = new SubstringBetween();
			String temp = sb.substringBetween(mstr, "national preparedness level", "national fire activity"); 
			national_prepareness_level = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
			temp = sb.substringBetween(mstr, "initial attack activity", "new large incidents"); 
			if (temp == null) temp = sb.substringBetween(mstr, "initial activity", "new large incidents"); 
			temp = (temp.substring(temp.indexOf(" ") + 1)).trim();	// remove all characters (such as :) before the first space and then trim
			if (temp.matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use Regex to check if this is just a number
				initial_attack_activity_number = temp;
				int num = Integer.valueOf(temp);
				if (num <= 199) {
					initial_attack_activity = "Light";
				} else if (num >= 200 && num <= 299) {
					initial_attack_activity = "Moderate";
				} else if (num >= 300) {
					initial_attack_activity = "Heavy";
				}
			} else {
				initial_attack_activity = temp.split(" ")[0];
				if (temp.split(" ").length > 1) {
					initial_attack_activity_number = (temp.substring(temp.lastIndexOf("(") + 1, temp.lastIndexOf(")")).replaceAll("new", "").replaceAll("fire", "").replaceAll("s", "")).trim();		// i.e. 20180915 is a special case
				} else {
					initial_attack_activity_number = "";
				}
			}
			initial_attack_activity = initial_attack_activity.substring(0, 1).toUpperCase() + initial_attack_activity.substring(1);
			temp = sb.substringBetween(mstr, "new large incidents", "large fires contained"); 
			new_large_incidents = (temp.substring(temp.indexOf(" ") + 1)).trim();
			temp = sb.substringBetween(mstr, "large fires contained", "uncontained large fires");
			large_fires_contained = (temp.substring(temp.indexOf(" ") + 1)).trim();
			temp = sb.substringBetween(mstr, "uncontained large fires", "area command teams committed");
			uncontained_large_fires = (temp.substring(temp.indexOf(" ") + 1)).trim();
			temp = sb.substringBetween(mstr, "area command teams committed", "nimos committed");
			area_command_teams_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
			temp = sb.substringBetween(mstr, "nimos committed", "type 1 imts committed");
			NIMOs_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
			temp = sb.substringBetween(mstr, "type 1 imts committed", "type 2 imts committed");
			type_1_IMTs_committed = (temp.substring(temp.indexOf(" ") + 1)).trim();
			type_2_IMTs_committed = (mstr.substring(mstr.lastIndexOf(" ") + 1)).trim();
			
			textarea.append(date + "\n");
			textarea.append(national_prepareness_level + "\n");
			textarea.append(initial_attack_activity + "\n");
			textarea.append(initial_attack_activity_number + "\n");
			textarea.append(new_large_incidents + "\n");
			textarea.append(large_fires_contained + "\n");
			textarea.append(uncontained_large_fires + "\n");
			textarea.append(area_command_teams_committed + "\n");
			textarea.append(NIMOs_committed + "\n");
			textarea.append(type_1_IMTs_committed + "\n");
			textarea.append(type_2_IMTs_committed + "\n");
			textarea.append("--------------------------------------------------------------------" + "\n");
			
			
			
			
			
			
			
			
			
			// The Format 1 of result when using adobe acrobat to convert from "pdf" to "text"		most data in the first 6 months		i.e. 20170106		(Note special case 20170203, I delete a row manually)
			if (lines[4].contains("National Preparedness Level ")) {
				textarea.append("Format 1. 20170106" + "\n");
				national_prepareness_level = lines[4].substring(lines[4].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[7].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[7];
					int num = Integer.valueOf(lines[7]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[7].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[7].substring(lines[7].lastIndexOf("(") + 1, lines[7].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[10];
				large_fires_contained = lines[13];
				uncontained_large_fires = lines[16];
				area_command_teams_committed = lines[19];
				NIMOs_committed = lines[22];
				type_1_IMTs_committed = lines[25];
				type_2_IMTs_committed = lines[28];
				get_data_type_multiple_lines();
			}
			
			// The Format 2 type of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20190329
			if (lines[0].contains("National Preparedness Level ") && lines[5].contains("Initial Attack Activity")) {
				textarea.append("Format 2. 20190329" + "\n");
				national_prepareness_level = lines[0].substring(lines[0].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[6].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[6];
					int num = Integer.valueOf(lines[6]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[6].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[6].substring(lines[6].lastIndexOf("(") + 1, lines[6].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[9];
				large_fires_contained = lines[12];
				uncontained_large_fires = lines[15];
				area_command_teams_committed = lines[18];
				NIMOs_committed = lines[21];
				type_1_IMTs_committed = lines[24];
				type_2_IMTs_committed = lines[27];
				get_data_type_multiple_lines();
			}
						
			// The Format 3 of result when using adobe acrobat to convert from "pdf" to "text"
			if (lines[0].contains("National Preparedness Level ") && lines[3].contains("Initial Attack Activity")) {
				textarea.append("Format 3. 20180813" + "\n");
				national_prepareness_level = lines[0].substring(lines[0].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[4].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[4];
					int num = Integer.valueOf(lines[4]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[4].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[4].substring(lines[4].lastIndexOf("(") + 1, lines[4].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[7];
				large_fires_contained = lines[10];
				uncontained_large_fires = lines[13];
				area_command_teams_committed = lines[16];
				NIMOs_committed = lines[19];
				type_1_IMTs_committed = lines[22];
				type_2_IMTs_committed = lines[25];
				get_data_type_multiple_lines();
			}
			
			
			// The Format 4 of result when using adobe acrobat to convert from "pdf" to "text"
			if (lines[3].contains("National Preparedness Level ") && lines[8].startsWith("New large incidents") && lines[11].startsWith("Large fires contained")) {
				textarea.append("Format 4. 20170524" + "\n");
				national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[6].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[6];
					int num = Integer.valueOf(lines[6]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[6].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[6].substring(lines[6].lastIndexOf("(") + 1, lines[6].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();		// i.e. 20180915 is a special case
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[9];
				large_fires_contained = lines[12];
				uncontained_large_fires = lines[15];
				area_command_teams_committed = lines[18];
				NIMOs_committed = lines[21];
				type_1_IMTs_committed = lines[24];
				type_2_IMTs_committed = lines[27];
				get_data_type_multiple_lines();
			}
			
			// The Format 5 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20190517
			if (lines[3].contains("National Preparedness Level ") && lines[4].isEmpty() && lines[5].isEmpty() && lines[6].isEmpty() && lines[9].startsWith("New large incidents")) {
				textarea.append("Format 5. 20190517" + "\n");
				national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
				String st = (lines[8].substring(lines[8].indexOf(" ") + 1)).trim();		// Note this should be location of first space + 1
				if (st.matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = st;
					int num = Integer.valueOf(st);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = st.split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (st.substring(st.lastIndexOf("(") + 1, st.lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = (lines[9].substring(lines[9].lastIndexOf(" ") + 1)).trim();
				large_fires_contained = (lines[10].substring(lines[10].lastIndexOf(" ") + 1)).trim();
				uncontained_large_fires = (lines[11].substring(lines[11].lastIndexOf(" ") + 1)).trim();;
				area_command_teams_committed = (lines[12].substring(lines[12].lastIndexOf(" ") + 1)).trim();
				NIMOs_committed = (lines[13].substring(lines[13].lastIndexOf(" ") + 1)).trim();
				type_1_IMTs_committed = (lines[14].substring(lines[14].lastIndexOf(" ") + 1)).trim();
				type_2_IMTs_committed = (lines[15].substring(lines[15].lastIndexOf(" ") + 1)).trim();
				get_data_type_multiple_lines();
			}
			
			// The Format 6 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20190921
			if (lines[3].contains("National Preparedness Level ") && lines[4].isEmpty() && lines[5].isEmpty() && lines[6].isEmpty() && lines[11].startsWith("New large incidents")) {
				textarea.append("Format 6. 20190921" + "\n");
				national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[9].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[9];
					int num = Integer.valueOf(lines[9]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[9].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[9].substring(lines[9].lastIndexOf("(") + 1, lines[9].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[12];
				large_fires_contained = lines[15];
				uncontained_large_fires = lines[18];
				area_command_teams_committed = lines[21];
				NIMOs_committed = lines[24];
				type_1_IMTs_committed = lines[27];
				type_2_IMTs_committed = lines[30];
				get_data_type_multiple_lines();
			}
			
			// The Format 7 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20170914
			if (lines[3].contains("National Preparedness Level ") && lines[8].isEmpty()) {
				textarea.append("Format 7. 20170914" + "\n");
				national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[7].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[7];
					int num = Integer.valueOf(lines[7]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[7].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[7].substring(lines[7].lastIndexOf("(") + 1, lines[7].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[10];
				large_fires_contained = lines[13];
				uncontained_large_fires = lines[16];
				area_command_teams_committed = lines[19];
				NIMOs_committed = lines[22];
				type_1_IMTs_committed = lines[25];
				type_2_IMTs_committed = lines[28];
				get_data_type_multiple_lines();
			}
			
			// The Format 8 of result when using adobe acrobat to convert from "pdf" to "text"		i.e. 20180510	20180511	20180512
			if (lines[3].contains("National Preparedness Level ") && lines[9].isEmpty()) {
				textarea.append("Format 8. 20180510, 20180511, 20180512" + "\n");
				national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[8].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[8];
					int num = Integer.valueOf(lines[8]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[8].split(" ")[0];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[8].substring(lines[8].lastIndexOf("(") + 1, lines[8].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[11];
				large_fires_contained = lines[14];
				uncontained_large_fires = lines[17];
				area_command_teams_committed = lines[20];
				NIMOs_committed = lines[23];
				type_1_IMTs_committed = lines[26];
				type_2_IMTs_committed = lines[29];
				get_data_type_multiple_lines();
			}
						
			// The Format 9 of result when using adobe acrobat to convert from "pdf" to "text"
			if (lines[3].contains("National Preparedness Level ") && lines[8].startsWith("Large fires contained")) {
				textarea.append("Format 9. " + "\n");
				national_prepareness_level = lines[3].substring(lines[3].lastIndexOf("National Preparedness Level ") + 28);
				if (lines[6].matches("-?(0|[1-9]\\d*)")) {		// this special case happens in several instance	i.e. 20180813, 20170331			use regex to check if this is just a number
					initial_attack_activity_number = lines[6];
					int num = Integer.valueOf(lines[6]);
					if (num <= 199) {
						initial_attack_activity = "Light";
					} else if (num >= 200 && num <= 299) {
						initial_attack_activity = "Moderate";
					} else if (num >= 300) {
						initial_attack_activity = "Heavy";
					}  
				} else {
					initial_attack_activity = lines[6].split(" ")[3];
					if (temp.split(" ").length > 1) {
						initial_attack_activity_number = (lines[6].substring(lines[6].lastIndexOf("(") + 1, lines[6].lastIndexOf(")")).replace("new", "").replace("fire", "").replace("s", "")).trim();
					} else {
						initial_attack_activity_number = "";
					}
				}
				new_large_incidents = lines[7].substring(lines[7].lastIndexOf(" ") + 1);
				large_fires_contained = lines[8].substring(lines[8].lastIndexOf(" ") + 1);
				uncontained_large_fires = lines[9].substring(lines[9].lastIndexOf(" ") + 1);
				area_command_teams_committed = lines[10].substring(lines[10].lastIndexOf(" ") + 1);
				NIMOs_committed = lines[11].substring(lines[11].lastIndexOf(" ") + 1);
				type_1_IMTs_committed = lines[12].substring(lines[12].lastIndexOf(" ") + 1);
				type_2_IMTs_committed = lines[13].substring(lines[13].lastIndexOf(" ") + 1);
				get_data_type_single_line();
			}
			
			
			
			
			textarea.setSelectionStart(0);	// scroll to top
			textarea.setSelectionEnd(0);
			lines_list = null; // free memory
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
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
		
		// Add the Panel to this Big ScrollPane------------------------------------------------------------------------------
		setBorder(BorderFactory.createEmptyBorder());
		setViewportView(explore_scrollpane);			
	}
	
	
	// Information of a Fire is in 15 lines
	private void get_data_type_multiple_lines() {
		textarea.append(date + "\n");
		textarea.append(national_prepareness_level + "\n");
		textarea.append(initial_attack_activity + "\n");
		textarea.append(initial_attack_activity_number + "\n");
		textarea.append(new_large_incidents + "\n");
		textarea.append(large_fires_contained + "\n");
		textarea.append(uncontained_large_fires + "\n");
		textarea.append(area_command_teams_committed + "\n");
		textarea.append(NIMOs_committed + "\n");
		textarea.append(type_1_IMTs_committed + "\n");
		textarea.append(type_2_IMTs_committed + "\n");
		
		// Loop all lines
		List<String> current_area = null;
		int count = 0;
		
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].startsWith("Alaska")) {
				current_area = new ArrayList<>(AICC);
			} else if (lines[i].startsWith("Eastern")) {
				current_area = new ArrayList<>(EACC);
			} else if (lines[i].startsWith("Great Basin")) {
				current_area = new ArrayList<>(GBCC);
			} else if (lines[i].startsWith("Northern California")) {
				current_area = new ArrayList<>(ONCC);
			} else if (lines[i].startsWith("Northern Rockies")) {
				current_area = new ArrayList<>(NRCC);
			} else if (lines[i].startsWith("Northwest")) {
				current_area = new ArrayList<>(NWCC);
			} else if (lines[i].startsWith("Rocky Mountain")) {
				current_area = new ArrayList<>(RMCC);
			} else if (lines[i].startsWith("Southern Area")) {
				current_area = new ArrayList<>(SACC);
			} else if (lines[i].startsWith("Southern California")) {
				current_area = new ArrayList<>(OSCC);
			} else if (lines[i].startsWith("Southwest")) {
				current_area = new ArrayList<>(SWCC);
			}
			
			if (lines[i].isEmpty() && count == 15 && lines[i - 14].toUpperCase().equals(lines[i - 14]) && lines[i - 14].contains("-")) {		// this is likely a fire, smart check based on the "unit" column
				String this_fire = String.join("\t", date, lines[i - 15], lines[i - 14], lines[i - 13], lines[i - 12], lines[i - 11],
														lines[i - 10], lines[i - 9], lines[i - 8], lines[i - 7], lines[i - 6],
														lines[i - 5], lines[i - 4], lines[i - 3], lines[i - 2], lines[i - 1]);
				current_area.add(this_fire);
				textarea.append(current_area.get(current_area.size() - 1) + "\n");
			}
			count = (lines[i].isEmpty()) ? 0 : (count + 1);	// increase count by 1 if not empty line
		}
	}
	
	
	// Information of a Fire is in one line
	private void get_data_type_single_line() {		// Note: need to fix the special case:	20180803  at page 10 where the table without header needs to be included   
		textarea.append(date + "\n");
		textarea.append(national_prepareness_level + "\n");
		textarea.append(initial_attack_activity + "\n");
		textarea.append(initial_attack_activity_number + "\n");
		textarea.append(new_large_incidents + "\n");
		textarea.append(large_fires_contained + "\n");
		textarea.append(uncontained_large_fires + "\n");
		textarea.append(area_command_teams_committed + "\n");
		textarea.append(NIMOs_committed + "\n");
		textarea.append(type_1_IMTs_committed + "\n");
		textarea.append(type_2_IMTs_committed + "\n");
		
		// Loop all lines
		List<String> current_area = null;
		int count = 0;
		do {
			if (lines[count].startsWith("Alaska")) {
				current_area = new ArrayList<>(AICC);
			} else if (lines[count].startsWith("Eastern")) {
				current_area = new ArrayList<>(EACC);
			} else if (lines[count].startsWith("Great Basin")) {
				current_area = new ArrayList<>(GBCC);
			} else if (lines[count].startsWith("Northern California")) {
				current_area = new ArrayList<>(ONCC);
			} else if (lines[count].startsWith("Northern Rockies")) {
				current_area = new ArrayList<>(NRCC);
			} else if (lines[count].startsWith("Northwest")) {
				current_area = new ArrayList<>(NWCC);
			} else if (lines[count].startsWith("Rocky Mountain")) {
				current_area = new ArrayList<>(RMCC);
			} else if (lines[count].startsWith("Southern Area")) {
				current_area = new ArrayList<>(SACC);
			} else if (lines[count].startsWith("Southern California")) {
				current_area = new ArrayList<>(OSCC);
			} else if (lines[count].startsWith("Southwest")) {
				current_area = new ArrayList<>(SWCC);
			}
			
			String[] line_split = lines[count].split(" ");
			int unit_id = 0;	// find the second column of the table
			int line_length = line_split.length;
			if (line_length >= 15 && line_split[line_length - 14].toUpperCase().equals(line_split[line_length - 14]) && line_split[line_length - 14].contains("-")) {		// this is likely a fire, smart check based on the "unit" column
				unit_id = line_split.length - 14;
				
				String this_fire = date;
				// this is the incident name, join by space
				for (int id = 0; id < unit_id; id++) {
					this_fire = String.join(" ", this_fire, line_split[id]);
				}
				// this is information in the whole line of this fire
				for (int id = unit_id; id < line_split.length; id++) {
					this_fire = String.join("\t", this_fire, line_split[id]);
				}

				current_area.add(this_fire);
				textarea.append(current_area.get(current_area.size() - 1) + "\n");
			}
			count = count + 1;
			
//			if (lines[count].startsWith("Acres Chge Total Chge Crw Eng Heli")) {
//				int total_fires = 0;		// including the lines "Large Fires Being Managed With a Strategy Other Than Full Suppression"
//				do {
//					total_fires = total_fires + 1;
//				} while (!lines[count + 1 + total_fires].isEmpty());	// while next line is not empty --> add one fire to total
//
//				for (int j = 0; j < total_fires; j++) {		// loop all lines that have fires
//					if (!lines[count + 1 + j].startsWith("Large Fires Being Managed With a Strategy Other Than Full Suppression")) {
//						String[] line_split = lines[count + 1 + j].split(" ");
//						int unit_id = 0;	// find the second column of the table
//						for (int id = 0; id < line_split.length; id++) {
//							if (line_split[id].contains("-")) {
//								unit_id = id;
//							}
//						}
//						
//						String this_fire = line_split[0];
//						// this is the incident name, join by space
//						for (int id = 1; id < unit_id; id++) {
//							this_fire = String.join(" ", this_fire, line_split[id]);
//						}
//						// this is information in the whole line of this fire
//						for (int id = unit_id; id < line_split.length; id++) {
//							this_fire = String.join("\t", this_fire, line_split[id]);
//						}
//
//						current_area.add(this_fire);
//						textarea.append(current_area.get(current_area.size() - 1) + "\n");
//					}
//				}
//				count = count + total_fires;
//			} else {
//				count = count + 1;
//			}
		} while (count < lines.length);
	}
}
