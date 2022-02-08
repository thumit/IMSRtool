package convenience_classes;
/*
Copyright (C) 2016-2020 PRISM Development Team

PRISM is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PRISM is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PRISM. If not, see <http://www.gnu.org/licenses/>.
*/



import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

public class TitleScrollPane extends JScrollPane {	
	// Scroll Panel with Title and the nested ScrollPane with Border
	private JScrollPane nested_scrollpane;
	private TitledBorder border;
	
	public TitleScrollPane(String title, String title_alignment, Component component) {
		nested_scrollpane = new JScrollPane(component);	
		nested_scrollpane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ColorUtil.makeTransparent(Color.BLACK, 75)));
		nested_scrollpane.setPreferredSize(new Dimension(100, 100));
		
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
