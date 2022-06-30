package root;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import sql.Calculate_Final_Ranking;
import sql.Calculate_Keyword_Frequency;

public class IMSRmain extends JFrame {
	// Define variables------------------------------------------------------------------------
	private static IMSRMenuBar 			menuBar;
	private JMenu 						menuUtility, menuHelp, menu_SIT_Ranking, menu_SIT_Keyword;
	private JMenuItem					extract, explore; 			// For menuUtility
	private JMenuItem 					content, update, about; 	// For menuHelp
	private JMenuItem 					total_points, A1, A2, A3, B1, B2, B3, C1, C2, C3, C4, D1, D2; 			// For SIT_Ranking
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
				// Define components: Menubar, Menus, MenuItems----------------------------------
				desktopPane = new IMSRDesktopPane();
				menuBar = new IMSRMenuBar();
				menuUtility = new JMenu("Utility");
				menuHelp = new JMenu("Help");
				menu_SIT_Keyword = new JMenu("SIT Keyword");
				menu_SIT_Ranking = new JMenu("SIT Ranking");
				
				explore = new JMenuItem("IMSR Explore ");
				extract = new JMenuItem("IMSR Extract");
				content = new JMenuItem("Content");
				update = new JMenuItem("Update");
				about = new JMenuItem("About");
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
				menuUtility.add(explore);
				menuUtility.add(extract);	
				menuUtility.add(menu_SIT_Keyword);
				menuUtility.add(menu_SIT_Ranking);	
				menuHelp.add(content);
				menuHelp.add(update);
				menuHelp.add(about);
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
				explore.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				explore.setMnemonic(KeyEvent.VK_O);
				explore.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Utility u = new Utility();
						u.explore_files();
					}
				});	
				
				extract.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				extract.setMnemonic(KeyEvent.VK_A);
				extract.addActionListener(new ActionListener() {
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
				
				total_points.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				total_points.setMnemonic(KeyEvent.VK_T);
				total_points.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						new Calculate_Final_Ranking();
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
		
		if (width >= 1600 && height >= 900) {
			return new Dimension(1600/2, 900/2);
		} else {
			return new Dimension((int) (width * 0.85/2), (int) (height * 0.85/2));
		}
	}
	
	public static IMSRDesktopPane get_DesktopPane() {
		return desktopPane;
	}
	
	public static IMSRmain get_main() {
		return main;
	}
}
