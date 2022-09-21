package root;
import javax.swing.JOptionPane;

public class OptionPane_ConfirmFireName extends JOptionPane {
	int response = -1;
	public OptionPane_ConfirmFireName(String date, String gacc_area, String[] optional_fire_names) {
		String title = date + " - " + gacc_area + ": Please select the correct fire name";
		boolean exit_pane = false;
		do {
			String ExitOption[] = { "NEXT", "AGGREGATE", "EXIT" };
			response = JOptionPane.showOptionDialog(IMSRmain.get_DesktopPane(), null, title,
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, optional_fire_names, optional_fire_names[0]);
			if (response >= 0) { // Next
				exit_pane = true;
			} else {
				exit_pane = false;
			}
		} while (exit_pane == false);
	}
}
