package root;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import convenience_classes.FilesChooser;
import convenience_classes.FilesHandle;


public class Utility {
	
	public void explore_files() {
		File[] s_files = FilesChooser.chosenTextFiles("Select simple2 text files"); // Open File chooser
		File[] r_files = FilesChooser.chosenTextFiles("Select raw text files"); // Open File chooser
		if (s_files != null && r_files != null) {
			new OptionPane_Explore(s_files, r_files);
		} else {
			JOptionPane.showMessageDialog(null, "Both simple2 text files and raw text files must be selected");
		}
	}
	
	public void convert_pdf_to_text_files() {
		File[] file = FilesChooser.chosenPdfFiles(); // Open File chooser
		if (file != null) {
			try {
				String inputFolder = file[0].getParentFile().toString();
				Path targetDirectory = Paths.get(inputFolder + "/pdftotext.exe");
				File pdftotext_exe_target_file = FilesHandle.getResourceFile("pdftotext.exe", targetDirectory);
				run_command(inputFolder, file, "simple2");	// Run command line
				run_command(inputFolder, file, "raw");		// Run command line
				// Delete the library file
				pdftotext_exe_target_file.deleteOnExit();
				if (pdftotext_exe_target_file.exists()) {
					pdftotext_exe_target_file.delete();
				}
				JOptionPane.showMessageDialog(null, file.length + " pdf files have been successfully converted to text files in 2 folders 'simple2' and 'raw'");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
	
	public void run_command(String inputFolder, File[] file, String corvert_option) {	// option = simple2 or raw		Note that we also use the option to create folder that contains all output text files
//		String convert_entire_folder_command = "for /r %i in (*.pdf) do \"pdftotext\" -simple2 \"%i\"";
//		String batch_command = "cd " + folder + " && " + convert_entire_folder_command;
//		//--------------------------------------------------------------------------------------------------------------------
//		// These code work but may cause the error "The command line is too long" when converting too many files in batch
//		String batch_command = "cd " + folder;
//		for (File f : file) {
//			batch_command = String.join(" && ", batch_command, "pdftotext -simple2 " + f.getName());
//		}
//		
//		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", batch_command);
//		builder.redirectErrorStream(true);
//		Process p = builder.start();
//		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//		String line;
//		while (true) {
//			line = r.readLine();
//			if (line == null) {
//				break;
//			}
//			System.out.println(line);
//		}
//		//--------------------------------------------------------------------------------------------------------------------
		File directory = new File(inputFolder);
		File out_directory = new File(inputFolder + "/" + corvert_option);	// use the option to create folder that contains all output text files
		if (!out_directory.exists()) {
			out_directory.mkdirs();
		}
		
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd " + inputFolder);	// we probably do not need these 2 lines, but sometimes it does not convert 1 file in below loop so I add these
		builder = builder.directory(directory);
		
//		for (File f : file) {
//			String command = "pdftotext -raw " + f.getName();
//			builder = new ProcessBuilder("cmd.exe", "/c", command);
//			builder = builder.directory(directory);
//			builder.redirectErrorStream(true);
//			Process p = builder.start();
//			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			String line;
//			while (true) {
//				line = r.readLine();
//				if (line == null) {
//					break;
//				}
//				System.out.println(line);
//			}
//		}
		
		// Same as above but using both batch command and multiple threads
		List<Thread> threads = new ArrayList<Thread>();
		int number_files_per_thread = 30;	 // simultaneously convert each * files
		int number_of_splits = file.length / number_files_per_thread;
		for (int i = 0; i <= number_of_splits; i++) {
			String batch_command = "cd " + inputFolder;
			for (int j = 0; j < number_files_per_thread; j++) {
				if (number_files_per_thread * i + j < file.length) {
					batch_command = String.join(" && ", batch_command, "pdftotext -" + corvert_option + " " + file[number_files_per_thread * i + j].getName()
							+ " " + corvert_option + "\\" + file[number_files_per_thread * i + j].getName().replace(".pdf", ".txt"));
				}
			}
			final String cmd = batch_command;
			// JOptionPane.showMessageDialog(null, cmd);
			Thread t = new Thread() {
				public void run() {
					ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
					builder.redirectErrorStream(true);
						try {
							Process p = builder.start();
							int exitVal = p.waitFor();		// very important to keep the initial process open until the batch file finished: https://stackoverflow.com/questions/6444812/executing-a-command-from-java-and-waiting-for-the-command-to-finish
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
				}
			};
			threads.add(t);
		}
		for (Thread t : threads) {
			t.start();
		}
		for (Thread t : threads) {
			try {
				t.join();	// Wait till all threads completes
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
}
