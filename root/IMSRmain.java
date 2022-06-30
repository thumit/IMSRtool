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

import sql.Calculate_Final_Ranking;

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
				
				menu_SIT_Keyword.setMnemonic(KeyEvent.VK_K);
				menu_SIT_Ranking.setMnemonic(KeyEvent.VK_R);
				
				total_points.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				total_points.setMnemonic(KeyEvent.VK_T);
				total_points.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Calculate_Final_Ranking sql = new Calculate_Final_Ranking();
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
