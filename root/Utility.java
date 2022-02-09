package root;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import convenience_classes.FilesChooser;


public class Utility {
	public File[] choose_files() {
		File[] files = FilesChooser.chosenFiles(); // Open File chooser
		if (files!= null) {
			// Loop through all files to get extension, match extension with delimited
			List<String> extentionList = new ArrayList<String>();	//A list contain all extension that have its delimited identified							
			List<String> delimitedList = new ArrayList<String>();	//A list contain all delimited. same structure as the extentionList	
				
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					File currentfile = files[i];
					String extension = "";
					String fileDelimited = "";
					int j = currentfile.getName().lastIndexOf('.');
					if (j > 0) {
						extension = currentfile.getName().substring(j + 1);
					}
					if (extension.equalsIgnoreCase("csv")) {
						fileDelimited = ",";
						extentionList.add("csv");
						delimitedList.add(",");
					} else if (!extentionList.contains(extension.toUpperCase())) {
//						// Choose the right delimited
//						// JDialog.setDefaultLookAndFeelDecorated(true);
//						UIManager.put("OptionPane.cancelButtonText", "Cancel");
//						UIManager.put("OptionPane.okButtonText", "Import");
//
//						Object[] selectionValues = { "Comma", "Space", "Tab" };
//						String initialSelection = "Comma";
//						String selection = (String) JOptionPane.showInputDialog(IMSRmain.get_DesktopPane(), "The delimited type for all '." + extension + "' files (i.e. " + currentfile.getName() + ") is",
//								"Specify delimited type", JOptionPane.QUESTION_MESSAGE, null, selectionValues, initialSelection);
//							
//						if (selection == "Comma") {
//							fileDelimited = ",";
//						} else if (selection == "Space") {
//							fileDelimited = "\\s+";
//						} else if (selection == "Tab") {
//							fileDelimited = "\t";
//						} else if (selection == null) {
//							fileDelimited = null;
//						}
//						extentionList.add(extension.toUpperCase());
//						delimitedList.add(fileDelimited);
//
//						UIManager.put("OptionPane.cancelButtonText", "Cancel");
//						UIManager.put("OptionPane.okButtonText", "Ok");
					}
					
//					try {		
//						// All lines to be in array
//						List<String> lines_list = Files.readAllLines(Paths.get(currentfile.getAbsolutePath()), StandardCharsets.UTF_8);
//						int lines_count = lines_list.size();
//						new OptionPane_Explore(currentfile);	
//						lines_list = null; // free memory
//					} catch (IOException e) {
//						System.err.println(e.getClass().getName() + ": " + e.getMessage());
//					}
					
				}
			}
		}
		return files;
	}
	
	public void explore_files () {
		File[] file = choose_files();
		new OptionPane_Explore(file);
	}
}
