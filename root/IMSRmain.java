package root;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import convenience_classes.FilesChooser;
import sql.Calculate_A1;
import sql.Calculate_A2;
import sql.Calculate_A3;
import sql.Calculate_B1;
import sql.Calculate_B2;
import sql.Calculate_B3;
import sql.Calculate_C1;
import sql.Calculate_C2;
import sql.Calculate_C3;
import sql.Calculate_C4;
import sql.Calculate_D1;
import sql.Calculate_D2;
import sql.Calculate_Final_Ranking;
import sql.Calculate_Keyword_Frequency;

public class IMSRmain extends JFrame {
	// Define variables------------------------------------------------------------------------
	private static IMSRMenuBar 			menuBar;
	private JMenu 						menuUtility, menuHelp, menu_SIT_Ranking, menu_SIT_Keyword;
	private JMenuItem					pdftotext, explore_extract, data_support; 			// For menuUtility
	private JMenuItem 					content, update, about; 	// For menuHelp
	private JMenuItem 					customization, total_points, A1, A2, A3, B1, B2, B3, C1, C2, C3, C4, D1, D2; 			// For SIT_Ranking
	private static IMSRDesktopPane 		desktopPane;
	private static IMSRContentPane 		contentPane;
	private static IMSRmain 			main;
	private static String 				version = "IMRS Tool 1.00";
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				main = new IMSRmain();
			}
		});
	}

	public IMSRmain() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {	
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {	// Set Look & Feel
					if (info.getName().equals("Nimbus")) {
						try {
							UIManager.setLookAndFeel(info.getClassName());
							String font_name = "Century Schoolbook";
							int font_size = 12;
							UIManager.getLookAndFeelDefaults().put("info", new Color(255, 250, 205));		// Change the ugly yellow color of ToolTip --> lemon chiffon
							UIManager.getLookAndFeelDefaults().put("defaultFont", new Font(font_name, Font.PLAIN, font_size));
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
							System.err.println(e1.getClass().getName() + ": " + e1.getMessage());
						}
						SwingUtilities.updateComponentTreeUI(main);
					}
				}	
						
				// Define components: Menubar, Menus, MenuItems----------------------------------
				desktopPane = new IMSRDesktopPane();
				menuBar = new IMSRMenuBar();
				menuUtility = new JMenu("Utility");
				menuHelp = new JMenu("Help");
				menu_SIT_Keyword = new JMenu("SIT Keyword");
				menu_SIT_Ranking = new JMenu("SIT Ranking");
				
				pdftotext = new JMenuItem("IMSR Pdf to Text ");
				explore_extract = new JMenuItem("IMSR Explore & Extract");
				data_support = new JMenuItem("Data Support Functions");
				content = new JMenuItem("Content");
				update = new JMenuItem("Update");
				about = new JMenuItem("About");
				customization = new JMenuItem("Customization");
				total_points = new JMenuItem("Total Points");
				A1 = new JMenuItem("A1 Points");
				A2 = new JMenuItem("A2 Points");
				A3 = new JMenuItem("A3 Points");
				B1 = new JMenuItem("B1 Points");
				B2 = new JMenuItem("B2 Points");
				B3 = new JMenuItem("B3 Points");
				C1 = new JMenuItem("C1 Points");
				C2 = new JMenuItem("C2 Points");
				C3 = new JMenuItem("C3 Points");
				C4 = new JMenuItem("C4 Points");
				D1 = new JMenuItem("D1 Points");
				D2 = new JMenuItem("D2 Points");
				
				// Add components: Menubar, Menus, MenuItems----------------------------------
				menuUtility.add(pdftotext);
				menuUtility.add(explore_extract);
				menuUtility.add(data_support);	
				menuUtility.add(menu_SIT_Keyword);
				menuUtility.add(menu_SIT_Ranking);	
				menuHelp.add(content);
				menuHelp.add(update);
				menuHelp.add(about);
				menu_SIT_Ranking.add(customization);
				menu_SIT_Ranking.add(total_points);
				menu_SIT_Ranking.add(A1);
				menu_SIT_Ranking.add(A2);
				menu_SIT_Ranking.add(A3);
				menu_SIT_Ranking.add(B1);
				menu_SIT_Ranking.add(B2);
				menu_SIT_Ranking.add(B3);
				menu_SIT_Ranking.add(C1);
				menu_SIT_Ranking.add(C2);
				menu_SIT_Ranking.add(C3);
				menu_SIT_Ranking.add(C4);
				menu_SIT_Ranking.add(D1);
				menu_SIT_Ranking.add(D2);

				menuBar.add(menuUtility);
				menuBar.add(menuHelp);
				setJMenuBar(menuBar);	
				
				contentPane = new IMSRContentPane();
		        contentPane.setLayout(new BorderLayout());	
		        contentPane.add(desktopPane);
		        setContentPane(contentPane);
		        
				pack();
				setLocationRelativeTo(null);
				setVisible(true);
				
				// Add listeners "New"------------------------------------------------
				pdftotext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				pdftotext.setMnemonic(KeyEvent.VK_P);
				pdftotext.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						File[] files = FilesChooser.chosenPdfFiles("Select pdf files for conversion to text files"); // Open File chooser
						Utility u = new Utility();
						u.convert_pdf_to_text_files(files);
					}
				});	
				
				explore_extract.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				explore_extract.setMnemonic(KeyEvent.VK_E);
				explore_extract.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Utility u = new Utility();
						u.explore_and_extract_files();
					}
				});	
				
				data_support.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				data_support.setMnemonic(KeyEvent.VK_D);
				data_support.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
					}
				});	
				
				menu_SIT_Ranking.setMnemonic(KeyEvent.VK_R);
				menu_SIT_Keyword.setMnemonic(KeyEvent.VK_K);
				menu_SIT_Keyword.addMenuListener(new MenuListener() {
					@Override
			        public void menuSelected(MenuEvent e) {					
						menu_SIT_Keyword.removeAll();			// Remove all existing projects
						String[] SIT_Field = new String[] { 
								"[SIGNIF_EVENTS_SUMMARY]", "[HAZARDS_MATLS_INVOLVMENT_NARR]", "[DAMAGE_ASSESSMENT_INFO]",
								"[LIFE_SAFETY_HEALTH_STATUS_NARR]", "[WEATHER_CONCERN_NARR]", 
								"[PROJECTED_ACTIVITY_12]", "[PROJECTED_ACTIVITY_24]", "[PROJECTED_ACTIVITY_48]", "[PROJECTED_ACTIVITY_72]", "[PROJECTED_ACTIVITY_GT72]",
								"[CURRENT_THREAT_12]", "[CURRENT_THREAT_24]", "[CURRENT_THREAT_48]", "[CURRENT_THREAT_72]", "[CURRENT_THREAT_GT72]",
								"[CRIT_RES_NEEDS_12]", "[CRIT_RES_NEEDS_24]", "[CRIT_RES_NEEDS_48]", "[CRIT_RES_NEEDS_72]", "[CRIT_RES_NEEDS_GT72]",	    	  
								"[STRATEGIC_DISCUSSION]", "[PLANNED_ACTIONS]", "[COMPLEXITY_LEVEL_NARR]", "[UNIT_OR_OTHER_NARR]", "[ADDTNL_COOP_ASSIST_ORG_NARR]"
						};
						for (int i = 0; i < SIT_Field.length; i++) {
							JMenuItem newJItem = new JMenuItem(SIT_Field[i]);
							menu_SIT_Keyword.add(newJItem);			// Add all existing projects
							String current_field = SIT_Field[i];
							newJItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent event) {
									new Calculate_Keyword_Frequency(current_field);
								}
							});
						}
					}

					@Override
					public void menuDeselected(MenuEvent e) {
					}

					@Override
					public void menuCanceled(MenuEvent e) {
					}
				});
				
				customization.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				customization.setMnemonic(KeyEvent.VK_C);
				customization.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						JInternalFrame frame = new JInternalFrame("SIT WILDFIRE PRIORITIZATION RANKING POINTS", true /*resizable*/, true, /*closable*/true/*maximizable*/, true/*iconifiable*/);	
						desktopPane.add(frame, BorderLayout.CENTER); // attach internal frame
						frame.setSize((int) (getWidth()/1.2),(int) (getHeight()/1.2));		
						frame.setLocation((int) ((getWidth() - frame.getWidth())/2),
														((int) ((getHeight() - frame.getHeight())/3.5)));	//Set the frame near the center of the Main frame
						if (main.get_DesktopPane().getSelectedFrame() != null) {	// Or set the frame near the recently opened JInternalFrame
							frame.setLocation(main.get_DesktopPane().getSelectedFrame().getX() + 25, main.get_DesktopPane().getSelectedFrame().getY() + 25);
						}
						frame.add(new SIT_Customization_Pane(), BorderLayout.CENTER);
						frame.setVisible(true); // show internal frame	
					}
				});	
				
				total_points.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				total_points.setMnemonic(KeyEvent.VK_T);
				total_points.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						new Calculate_Final_Ranking(Arrays.asList("2015", "2016"), Arrays.asList("A1", "D2"));
					}
				});	
				
				A1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_A1 A1 = new Calculate_A1(Arrays.asList("2015", "2016"));
						A1.show_A1_scroll();
					}
				});
				
				A2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_A2 A2 = new Calculate_A2(Arrays.asList("2015", "2016"));
						A2.show_A2_scroll();
					}
				});	
				
				A3.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_A3 A3 = new Calculate_A3(Arrays.asList("2015", "2016"));
						A3.show_A3_scroll();
					}
				});	
				
				B1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_B1 B1 = new Calculate_B1(Arrays.asList("2015", "2016"));
						B1.show_B1_scroll();
					}
				});	
				
				B2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_B2 B2 = new Calculate_B2(Arrays.asList("2015", "2016"));
						B2.show_B2_scroll();
					}
				});	
				
				B3.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_B3 B3 = new Calculate_B3(Arrays.asList("2015", "2016"));
						B3.show_B3_scroll();
					}
				});	
				
				C1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_C1 C1 = new Calculate_C1(Arrays.asList("2015", "2016"));
						C1.show_C1_scroll();
					}
				});	
				
				C2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_C2 C2 = new Calculate_C2(Arrays.asList("2015", "2016"));
						C2.show_C2_scroll();
					}
				});	
				
				C3.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_C3 C3 = new Calculate_C3(Arrays.asList("2015", "2016"));
						C3.show_C3_scroll();
					}
				});	
				
				C4.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_C4 C4 = new Calculate_C4(Arrays.asList("2015", "2016"));
						C4.show_C4_scroll();
					}
				});	
				
				D1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_D1 D1 = new Calculate_D1(Arrays.asList("2015", "2016"));
					}
				});	
				
				D2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_D2 D2 = new Calculate_D2(Arrays.asList("2015", "2016"));
					}
				});	
			}
		});
	}
			
	
	//--------------------------------------------------------------------------------------------------------------------------------
	@Override
	public Dimension getPreferredSize() {
		// Check multi-monitor screen resolution
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		return new Dimension((int) (width * 0.8), (int) (height * 0.9));
	}
	
	public static IMSRDesktopPane get_DesktopPane() {
		return desktopPane;
	}
	
	public static IMSRmain get_main() {
		return main;
	}
}
