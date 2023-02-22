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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import root.IMSRmain;

public class TitleScrollPane extends JScrollPane {	
	// Scroll Panel with Title and the nested ScrollPane with Border
	private JScrollPane nested_scrollpane;
	private TitledBorder border;
	
	public TitleScrollPane(String title, String title_alignment, Component component) {
		nested_scrollpane = new JScrollPane(component);	
		nested_scrollpane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ColorUtil.makeTransparent(Color.BLACK, 75)));
		nested_scrollpane.setPreferredSize(new Dimension(100, 100));
		nested_scrollpane.addHierarchyListener(new HierarchyListener() {	//	These codes make the license_scrollpane resizable --> the Big ScrollPane resizable --> JOptionPane resizable
		    public void hierarchyChanged(HierarchyEvent e) {
		        Window window = SwingUtilities.getWindowAncestor(nested_scrollpane);
		        if (window instanceof Dialog) {
		            Dialog dialog = (Dialog)window;
		            if (!dialog.isResizable()) {
		                dialog.setResizable(true);
		                dialog.setPreferredSize(new Dimension((int) (IMSRmain.get_main().getWidth() / 1.1), (int) (IMSRmain.get_main().getHeight() / 1.21)));
		            }
		        }
		    }
		});
		
		border = new TitledBorder(title);
		if (title_alignment.equals("LEFT")) {
			border.setTitleJustification(TitledBorder.LEFT);
		} else if (title_alignment.equals("RIGHT")) {
			border.setTitleJustification(TitledBorder.RIGHT);
		}
		else if (title_alignment.equals("CENTER")) {
			border.setTitleJustification(TitledBorder.CENTER);
		}		
		setBorder(border);
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);	
		setViewportView(nested_scrollpane);
	}
	
	public JScrollPane get_nested_scrollpane() {
		return nested_scrollpane;	
	}
	
	public void set_title(String title) {
		border.setTitle(title);	
	}
}
