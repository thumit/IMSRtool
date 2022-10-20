package root;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import convenience_classes.GridBagLayoutHandle;
import convenience_classes.TextAreaReadMe;
import convenience_classes.TitleScrollPane;

public class OptionPane_Explore extends JOptionPane {
	public OptionPane_Explore(File[] pdf_files, File[] s_files, File[] r_files) {
		int id = 0;
		boolean exit_exploration = false;
		do {
			Explore_Pane explore_pane = new Explore_Pane(pdf_files[id], s_files[id], r_files[id]);
			JScrollPane scroll = new JScrollPane();
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.setViewportView(explore_pane);
//			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

			String ExitOption[] = { "PREVIOUS", "NEXT", "AGGREGATE", "EXIT" };
			int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), scroll, "EXPLORE",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
			if (response == 0) { // Previous
				if (id > 0) id = id - 1;
			} else if (response == 1) { // Next
				if (id < s_files.length - 1) id = id + 1;
			} else if (response == 2) {
				exit_exploration = true;
				new Aggregate_Scroll(s_files, r_files); // Aggregate
			} else {
				exit_exploration = true;
			}
		} while (exit_exploration == false && id < s_files.length);
	}
}

class Aggregate_Scroll extends JScrollPane {
	public Aggregate_Scroll(File[] s_files, File[] r_files) {		
		String[] header1 = new String[] { "date", "national_prepareness_level", "initial_attack_activity",
				"initial_attack_new_fires", "new_large_incidents", "large_fires_contained",
				"uncontained_large_fires", "area_command_teams_committed", "nimos_committed", "type_1_imts_committed",
				"type_2_imts_committed" };
		String[] header2 = new String[] { "date", "gacc", "gacc_priority", "gacc_prepareness_level", "gacc_new_fires",
				"gacc_new_large_incidents", "gacc_uncontained_large_fires", "gacc_area_command_teams_committed",
				"gacc_nimos_committed", "gacc_type_1_imts_committed", "gacc_type_2_imts_committed" };
		String[] header3 = new String[] { "date", "gacc", "gacc_priority", "fire_priority", "incident_name", "unit", "size_acres",
				"size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw",
				"resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own" };
		String[] header4 = new String[] { "date", "gacc_name", "incidents", "cumulative_acres", "crews", "engines", "helicopters", "total_personnel", "change_in_personnel" };
		
		ISMR_Process[] ismr_process = new ISMR_Process[s_files.length];
		for (int i = 0; i < s_files.length; i++) {
			ismr_process[i] = new ISMR_Process(s_files[i], r_files[i]);
		}
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
		textarea.append(String.join("\t", header1)  + "\n");
		for (ISMR_Process ismr : ismr_process) {
			textarea.append(String.join("\t", ismr.national_fire_activity)  + "\n");
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header2)  + "\n");
		for (ISMR_Process ismr : ismr_process) {
			for (String st : ismr.gacc_fire_activity) {
				textarea.append(st + "\n");
			}
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header3)  + "\n");
		for (ISMR_Process ismr : ismr_process) {
			for (String fire : ismr.all_fires) {
				textarea.append(fire + "\n");
			}
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		for (ISMR_Process ismr : ismr_process) {
			if (ismr.all_fires.isEmpty()) {
				textarea.append(ismr.date + " has no fire" + "\n");
			}
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header4)  + "\n");
		for (ISMR_Process ismr : ismr_process) {
			for (String st : ismr.resource_summary) {
				textarea.append(st + "\n");
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
		String ExitOption[] = { "EXPORT TEXT FILE", "EXPORT DATABASE FILE", "EXPORT TO SQLSERVER", "EXIT" };
		int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), this, "AGRREGATION PREVIEW",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, ExitOption, ExitOption[0]);
		if (response == 0) {
			
		} else if (response == 1) {
			
		} else {
		}
	}
}

class Explore_Pane extends JSplitPane {
	public Explore_Pane(File pdf, File s_file, File r_file) {	
		String original_title = s_file.getName().toString().replace(".txt", ".pdf") + " - ORIGINAL";
		ScrollPane_View_PDF original_pdf = new ScrollPane_View_PDF(pdf, original_title);
		
		String s_title = s_file.getName().toString() + " - SIMPLE2 CONVERSION";
		ScrollPane_View_Trim_File s_trim = new ScrollPane_View_Trim_File(s_file, s_title);
		
		String r_title = s_file.getName().toString() + " - RAW CONVERSION";
		ScrollPane_View_Trim_File r_trim = new ScrollPane_View_Trim_File(r_file, r_title);
		
		JPanel radio_panel = new JPanel();
		radio_panel.setLayout(new FlowLayout());	
		ButtonGroup radio_button_group = new ButtonGroup();
		JRadioButton[] radio_button = new JRadioButton[3];
		radio_button[0]= new JRadioButton("ORIGINAL");
		radio_button[1]= new JRadioButton("RAW");
		radio_button[2]= new JRadioButton("SIMPLE2");
		for (int i = 0; i < radio_button.length; i++) {
				radio_button_group.add(radio_button[i]);
				radio_panel.add(radio_button[i]);
		}	
		JScrollPane view = new JScrollPane(s_trim);
		view.setBorder(BorderFactory.createEmptyBorder());
		radio_button[0].addActionListener(e -> {
			view.setViewportView(original_pdf);
			try {
				Desktop.getDesktop().open(pdf);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
		radio_button[1].addActionListener(e -> {
			view.setViewportView(r_trim);
		});
		radio_button[2].addActionListener(e -> {
			view.setViewportView(s_trim);
		});
		radio_button[2].setSelected(true);
		
		JPanel combine_panel_1 = new JPanel();
		combine_panel_1.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		combine_panel_1.add(view, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		combine_panel_1.add(radio_panel, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 1, 1, 1, 1, 0, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		
		String preview_title = s_file.getName().toString() + " - EXTRACTION PREVIEW";
		ScrollPane_Extraction_Preview preview = new ScrollPane_Extraction_Preview(s_file, r_file, preview_title);
		// Just to align with left split screen
		JPanel radio_panel_2 = new JPanel();
		JRadioButton radio_button_2 = new JRadioButton("");
		radio_button_2.setEnabled(false);
		radio_panel_2.add(radio_button_2);
		JPanel combine_panel_2 = new JPanel();
		combine_panel_2.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		combine_panel_2.add(preview, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		combine_panel_2.add(radio_panel_2, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 1, 1, 1, 1, 0, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		
		setBorder(BorderFactory.createEmptyBorder());
		setResizeWeight(0.5);
		setDividerSize(3);
//		setDividerLocation(250);
		setOneTouchExpandable(true);
		setLeftComponent(combine_panel_1);
		setRightComponent(combine_panel_2);
	}
}

class ScrollPane_View_PDF extends JScrollPane {
	public ScrollPane_View_PDF(File pdf, String title) {
		// download version 7.0.0: https://stackoverflow.com/questions/4437910/java-pdf-viewer
		// guide: https://jar-download.com/download-handling.php
		// Note: Need to add all dependencies, it would be 28MBs

//		// build a controller
//		SwingController controller = new SwingController();
//		controller.setToolBarVisible(false);
//		controller.setPageFitMode(DocumentViewController.PAGE_FIT_WINDOW_HEIGHT, false);
//		// Build a SwingViewFactory configured with the controller
//		SwingViewBuilder factory = new SwingViewBuilder(controller);
//		// Use the factory to build a JPanel that is pre-configured with a complete, active Viewer UI.
//		JPanel viewerComponentPanel = factory.buildViewerPanel();
//		// add copy keyboard command
//		ComponentKeyBinding.install(controller, viewerComponentPanel);
//		// add interactive mouse link annotation support via callback
//		controller.getDocumentViewController().setAnnotationCallback(
//		      new org.icepdf.ri.common.MyAnnotationCallback(
//		             controller.getDocumentViewController()));
//		// Open a PDF document to view
//        controller.openDocument(pdf.getAbsolutePath());
//        setViewportView(viewerComponentPanel);
        
        TitledBorder border = new TitledBorder(title);
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);
//		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
	}
}


class ScrollPane_View_File extends JScrollPane {
	public ScrollPane_View_File(File file, String title) {	
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
		
		TitleScrollPane explore_scrollpane = new TitleScrollPane(title, "CENTER", textarea);
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

class ScrollPane_View_Trim_File extends JScrollPane {
	public ScrollPane_View_Trim_File(File file, String title) {	
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
		
		TitleScrollPane explore_scrollpane = new TitleScrollPane(title, "CENTER", textarea);
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

class ScrollPane_Extraction_Preview extends JScrollPane {
	public ScrollPane_Extraction_Preview(File s_file, File r_file, String title) {
		String[] header1 = new String[] { "date", "national_prepareness_level", "initial_attack_activity",
				"initial_attack_new_fires", "new_large_incidents", "large_fires_contained",
				"uncontained_large_fires", "area_command_teams_committed", "nimos_committed", "type_1_imts_committed",
				"type_2_imts_committed" };
		String[] header2 = new String[] { "date", "gacc", "gacc_priority", "gacc_prepareness_level", "gacc_new_fires",
				"gacc_new_large_incidents", "gacc_uncontained_large_fires", "gacc_area_command_teams_committed",
				"gacc_nimos_committed", "gacc_type_1_imts_committed", "gacc_type_2_imts_committed" };
		String[] header3 = new String[] { "date", "gacc", "gacc_priority", "fire_priority", "incident_name", "unit", "size_acres",
				"size_chge", "percentage", "ctn_comp", "est", "personnel_total", "personnel_chge", "resources_crw",
				"resources_eng", "resources_heli", "strc_lost", "ctd", "origin_own" };
		String[] header4 = new String[] { "date", "gacc_name", "incidents", "cumulative_acres", "crews", "engines", "helicopters", "total_personnel", "change_in_personnel" };
		
		TextAreaReadMe textarea = new TextAreaReadMe("icon_tree.png", 75, 75);	// Print to text area
		ISMR_Process ismr = new ISMR_Process(s_file, r_file);
		textarea.append(ismr.date + "\n");
		textarea.append(ismr.national_prepareness_level + "\n");
		textarea.append(ismr.initial_attack_activity + "\n");
		textarea.append(ismr.initial_attack_new_fires + "\n");
		textarea.append(ismr.new_large_incidents + "\n");
		textarea.append(ismr.large_fires_contained + "\n");
		textarea.append(ismr.uncontained_large_fires + "\n");
		textarea.append(ismr.area_command_teams_committed + "\n");
		textarea.append(ismr.nimos_committed + "\n");
		textarea.append(ismr.type_1_imts_committed + "\n");
		textarea.append(ismr.type_2_imts_committed + "\n");
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header2)  + "\n");
		for (String st : ismr.gacc_fire_activity) {
			textarea.append(st + "\n");
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header3)  + "\n");
		for (String fire : ismr.all_fires) {
			textarea.append(fire + "\n");
		}
		textarea.append("--------------------------------------------------------------------" + "\n");
		textarea.append(String.join("\t", header4)  + "\n");
		for (String st : ismr.resource_summary) {
			textarea.append(st + "\n");
		}
		textarea.setSelectionStart(0);	// scroll to top
		textarea.setSelectionEnd(0);
		textarea.setEditable(false);
		
		TitleScrollPane explore_scrollpane = new TitleScrollPane(title, "CENTER", textarea);
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
