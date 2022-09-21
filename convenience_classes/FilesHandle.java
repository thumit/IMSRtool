package convenience_classes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import root.IMSRmain;

public class FilesHandle {
	public FilesHandle() {
	}
	
	
	public static String get_workingLocation() {
		// Get working location of spectrumLite
		String workingLocation;

		// Get working location of the IDE project, or runnable jar file
		final File jarFile = new File(IMSRmain.get_main().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		workingLocation = jarFile.getParentFile().toString();

		// Make the working location with correct name
		try {
			// to handle name with space (%20)
			workingLocation = URLDecoder.decode(workingLocation, "utf-8");
			workingLocation = new File(workingLocation).getPath();
		} catch (UnsupportedEncodingException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}

		return workingLocation;
	}

	
	public static File get_temporaryFolder() {		
		String workingLocation = get_workingLocation();
		File temporaryFolder = new File(workingLocation + "/Temporary");
		if (!temporaryFolder.exists()) {
			temporaryFolder.mkdirs();
		} // Create folder Temporary if it does not exist
		return temporaryFolder;
	}	
	
	
//	public static File chosenDefinition() {
//		File file = null;
//			
//		ImageIcon icon = new ImageIcon(IMSRmain.get_main().getClass().getResource("/icon_question.png"));
//		Image scaleImage = icon.getImage().getScaledInstance(50, 50,Image.SCALE_SMOOTH);
//		String ExitOption[] = {"New definition","Default definition","Cancel"};
//		int response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(),"Except General Inputs, everything will be reset. Your option ?", "Import Strata Definition",
//				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, new ImageIcon(scaleImage), ExitOption, ExitOption[2]);
//		if (response == 0)
//		{
//			JFileChooser chooser = new JFileChooser();
//			chooser.setPreferredSize(new Dimension(800, 500));
//			chooser.setCurrentDirectory(new File(get_workingLocation()));
//			chooser.setDialogTitle("Select strata definition file");
//			chooser.setMultiSelectionEnabled(false);
//			
//			chooser.setApproveButtonText("Import");
//			chooser.setApproveButtonToolTipText("Import strata definition from the selected file");
//			FileNameExtensionFilter filter = new FileNameExtensionFilter("Strata Definition File '.csv' '.txt'", "csv", "txt");
//			chooser.setFileFilter(filter);
//			chooser.setAcceptAllFileFilterUsed(false);
//			
//			int returnValue = chooser.showOpenDialog(IMSRmain.get_main());
//			if (returnValue == JFileChooser.APPROVE_OPTION) {	//Return the new Definition as in the selected file
//				file = chooser.getSelectedFile();
//			}
//		}
//		if (response == 1)	
//		{
//			try {
//				File file_StrataDefinition = new File(FilesHandle.get_temporaryFolder().getAbsolutePath() + "/" + "strata_definition.csv");	
//				file_StrataDefinition.deleteOnExit();
//					
//				InputStream initialStream = Panel_Edit_Details.class.getResourceAsStream("/strata_definition.csv");		//Default definition
//				byte[] buffer = new byte[initialStream.available()];
//				initialStream.read(buffer);
//
//				OutputStream outStream = new FileOutputStream(file_StrataDefinition);
//				outStream.write(buffer);
//
//				initialStream.close();
//				outStream.close();
//
//				file = file_StrataDefinition;
//			} catch (FileNotFoundException e1) {
//				System.err.println(e1.getClass().getName() + ": " + e1.getMessage());
//			} catch (IOException e2) {
//				System.err.println(e2.getClass().getName() + ": " + e2.getMessage());
//			}
//		}	
//		
//		return file;
//	}	
//	
//	
//	public static File chosenDatabase() {
//		JFileChooser chooser = new JFileChooser();
//		chooser.setPreferredSize(new Dimension(800, 500));
//		chooser.setCurrentDirectory(get_databasesFolder());
//		chooser.setDialogTitle("Select database file");
//		chooser.setMultiSelectionEnabled(false);
//		
//		chooser.setApproveButtonText("Import");
//		chooser.setApproveButtonToolTipText("Import database of the existing strata from the selected file");
//		FileNameExtensionFilter filter = new FileNameExtensionFilter("Database file '.db'", "db");
//		chooser.setFileFilter(filter);
//		chooser.setAcceptAllFileFilterUsed(false);
//		
//		int returnValue = chooser.showOpenDialog(IMSRmain.get_main());
//		File file = null;
//		if (returnValue == JFileChooser.APPROVE_OPTION) {
//			file = chooser.getSelectedFile();
//		}
//
//		return file;
//	}			

	
	public static File get_file_maequee() {
		// Read maequee from the system
		File file_maequee = null;
		try {
			file_maequee = new File(get_temporaryFolder().getAbsolutePath() + "/" + "maequee.txt");
			file_maequee.deleteOnExit();

			InputStream initialStream = IMSRmain.get_main().getClass().getResourceAsStream("/maequee.txt");
			byte[] buffer = new byte[initialStream.available()];
			initialStream.read(buffer);

			OutputStream outStream = new FileOutputStream(file_maequee);
			outStream.write(buffer);

			initialStream.close();
			outStream.close();
		} catch (FileNotFoundException e1) {
			System.err.println(e1.getClass().getName() + ": " + e1.getMessage());
		} catch (IOException e2) {
			System.err.println(e2.getClass().getName() + ": " + e2.getMessage());
		} 
		return file_maequee;
	}
	
	public static File get_file_from_resource(String file_name) {
		URL url = IMSRmain.get_main().getClass().getResource("/" + file_name);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
}
