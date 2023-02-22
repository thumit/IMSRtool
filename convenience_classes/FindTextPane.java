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
package convenience_classes;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

public class FindTextPane extends JPanel {
	private JTextField findField;
	private JButton findButton;
	private int pos = 0;

	public FindTextPane(ColorTextArea textarea) {	// https://stackoverflow.com/questions/13437865/java-scroll-to-specific-text-inside-jtextarea
		findButton = new JButton("FIND NEXT");
		findField = new JTextField("", 5);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		add(findField, GridBagLayoutHandle.get_c(c, "BOTH", 
				0, 0, 1, 1, 1, 1, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right
		add(findButton, GridBagLayoutHandle.get_c(c, "BOTH", 
				1, 0, 1, 1, 0, 0, 	// gridx, gridy, gridwidth, gridheight, weightx, weighty
				0, 0, 0, 0));		// insets top, left, bottom, right

		findButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get the text to find...convert it to lower case for easier comparison
				String find = findField.getText().toLowerCase();
				// Focus the text area, otherwise the highlighting won't show up
				textarea.requestFocusInWindow();
				// Make sure we have a valid search term
				if (find != null && find.length() > 0) {
					Document document = textarea.getDocument();
					int findLength = find.length();
					try {
						boolean found = false;
						// Rest the search position if we're at the end of the document
						if (pos + findLength > document.getLength()) {
							pos = 0;
						}
						// While we haven't reached the end..."<=" Correction
						while (pos + findLength <= document.getLength()) {
							// Extract the text from the document
							String match = document.getText(pos, findLength).toLowerCase();
							// Check to see if it matches or request
							if (match.equals(find)) {
								found = true;
								break;
							}
							pos++;
						}
						// Did we find something...
						if (found) {
							// Get the rectangle of the where the text would be visible...
							Rectangle viewRect = textarea.modelToView(pos);
							// Scroll to make the rectangle visible
							textarea.scrollRectToVisible(viewRect);
							// Highlight the text
							textarea.setCaretPosition(pos + findLength);
							textarea.moveCaretPosition(pos);
							// Move the search position beyond the current match
							pos += findLength;
						}
					} catch (Exception exp) {
						exp.printStackTrace();
					}

				}
			}
		});

	}
}