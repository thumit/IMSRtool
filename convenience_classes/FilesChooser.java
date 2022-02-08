package convenience_classes;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import root.IMSRmain;

public class FilesChooser {
	
	public static File[] chosenFiles() {
		JFileChooser chooser = new JFileChooser("Select files to explore");
		chooser.setPreferredSize(new Dimension(800, 500));
//		chooser.setCurrentDirectory(new File(FilesHandle.get_workingLocation()));
		chooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory().getAbsoluteFile());	// Desktop Path
		chooser.setMultiSelectionEnabled(true);
		chooser.setApproveButtonText("Explore");
		chooser.setApproveButtonToolTipText("Explore files");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("text file", "txt", "csv");
		chooser.setFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		
		int returnValue = chooser.showOpenDialog(IMSRmain.get_DesktopPane());
		File[] files = null;
		if (returnValue == JFileChooser.APPROVE_OPTION) {

			files = chooser.getSelectedFiles();
		}
		return files;
	}
	
	public static File[] chosenDatabases() {
		JFileChooser chooser = new JFileChooser("Select .db files to be imported as databasses");
		chooser.setPreferredSize(new Dimension(800, 500));
		chooser.setCurrentDirectory(new File(FilesHandle.get_workingLocation()));
		chooser.setMultiSelectionEnabled(true);
		chooser.setApproveButtonText("Import");
		chooser.setApproveButtonToolTipText("Import files as databases");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Data Base File '.db'", "db");
		chooser.setFileFilter(filter);
		chooser.setAcceptAllFileFilterUsed(false);
		
		int returnValue = chooser.showOpenDialog(IMSRmain.get_DesktopPane());
		File[] files = null;
		if (returnValue == JFileChooser.APPROVE_OPTION) {

			files = chooser.getSelectedFiles();
		}
		return files;
	}
}
