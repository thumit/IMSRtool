/*
Copyright (C) 2022-2023 IMSR-TOOL DEVELOPER

IMSR-TOOL is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

IMSR-TOOL is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with IMSR-TOOL. If not, see <http://www.gnu.org/licenses/>.
*/
package root;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import convenience_classes.FilesChooser;
import convenience_classes.FilesHandle;

public class IMSRmain extends JFrame {
	// Define variables------------------------------------------------------------------------
	private static IMSRMenuBar 			menuBar;
	private JMenu 						menuUtility, menuHelp;
	private JMenuItem					pdftotext, explore_extract; // For menuUtility
	private JMenuItem 					user_manual, about; 	// For menuHelp
	private static IMSRDesktopPane 		desktopPane;
	private static IMSRContentPane 		contentPane;
	private static IMSRmain 			main;
	private static String 				version = "IMSR-TOOL-1.08";
	
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
				
				pdftotext = new JMenuItem("IMSR Pdf to Text ");	pdftotext.setVisible(false);
				explore_extract = new JMenuItem("IMSR Explore & Extract");
				user_manual = new JMenuItem("User Manual");
				about = new JMenuItem("License");
				
				// Add components: Menubar, Menus, MenuItems----------------------------------
				menuUtility.add(pdftotext);
				menuUtility.add(explore_extract);
				menuHelp.add(user_manual);
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
				
				// Add listeners------------------------------------------------
//				pdftotext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				pdftotext.setMnemonic(KeyEvent.VK_P);
				pdftotext.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						File[] files = FilesChooser.chosenPdfFiles("Select pdf files for conversion to text files"); // Open File chooser
						Utility u = new Utility();
						u.convert_pdf_to_text_files(files);
					}
				});	
				
//				explore_extract.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				explore_extract.setMnemonic(KeyEvent.VK_E);
				explore_extract.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Utility u = new Utility();
						u.explore_and_extract_files();
					}
				});	
				
//				user_manual.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));	// CTRL on Windows, *** on MAC-OS
				user_manual.setMnemonic(KeyEvent.VK_U);
				user_manual.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						File user_manual_pdf = null;
						if (FilesHandle.executed_by_jar()) {
							Path targetDirectory = Paths.get(FilesHandle.get_workingLocation() + "/" + version + "-USER-MANUAL.pdf");
							user_manual_pdf = FilesHandle.getResourceFile(version + "-USER-MANUAL.pdf", targetDirectory);
							if (!user_manual_pdf.exists()) JOptionPane.showMessageDialog(null, version + "-USER-MANUAL.pdf has been copied to " + user_manual_pdf.getAbsolutePath());
						} else {
							user_manual_pdf = FilesHandle.get_file_from_resource(version + "-USER-MANUAL.pdf");
						}
						
						try {
							Desktop.getDesktop().open(user_manual_pdf);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});			
				
				about.setMnemonic(KeyEvent.VK_U);
				about.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						File license_file = null;
						if (FilesHandle.executed_by_jar()) {
							Path targetDirectory = Paths.get(FilesHandle.get_workingLocation() + "/license-GPL.txt");
							license_file = FilesHandle.getResourceFile("license-GPL.txt", targetDirectory);
							if (!license_file.exists()) JOptionPane.showMessageDialog(null, "license-GPL.txt has been copied to " + license_file.getAbsolutePath());
						} else {
							license_file = FilesHandle.get_file_from_resource("license-GPL.txt");
						}
						
						try {
							Desktop.getDesktop().open(license_file);
						} catch (IOException e) {
							e.printStackTrace();
						}
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
