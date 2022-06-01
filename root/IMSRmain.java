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

public class IMSRmain extends JFrame {
	// Define variables------------------------------------------------------------------------
	private static IMSRMenuBar 			menuBar;
	private JMenu 						menuUtility, menuHelp;
	private JMenuItem					extract, explore, sqlserver; 	// For menuUtility
	private JMenuItem 					content, update, about; 	// For menuHelp
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
				
				explore = new JMenuItem("Explore");
				extract = new JMenuItem("Extract");
				sqlserver = new JMenuItem("SQLserver");
				content = new JMenuItem("Content");
				update = new JMenuItem("Update");
				about = new JMenuItem("About");
				
				// Add components: Menubar, Menus, MenuItems----------------------------------
				menuUtility.add(explore);
				menuUtility.add(extract);		
				menuUtility.add(sqlserver);
				menuHelp.add(content);
				menuHelp.add(update);
				menuHelp.add(about);

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
				extract.setMnemonic(KeyEvent.VK_E);
				extract.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
					}
				});	
				
				sqlserver.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				sqlserver.setMnemonic(KeyEvent.VK_S);
				sqlserver.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						SQLserver sql = new SQLserver();
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
