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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;

import convenience_classes.ColorUtil;
import sql.Calculate_Final_Ranking;

public class SIT_Customization_Pane extends JLayeredPane {
	private JSplitPane splitPanel;
	private JScrollPane scrollPane_Left;
	private JScrollPane scrollPane_Right;
	
	public SIT_Customization_Pane() {
		this.setLayout(new BorderLayout(0, 0));
		ToolTipManager.sharedInstance().setInitialDelay(0);		//Show toolTip immediately

		splitPanel = new JSplitPane();
//		splitPanel.setResizeWeight(0.15);
		splitPanel.setOneTouchExpandable(true);
		splitPanel.setDividerLocation(220);
		
		// Left split panel--------------------------------------------------------------------------------
		scrollPane_Left = new JScrollPane(new INPUT_PANE());
		scrollPane_Left.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ColorUtil.makeTransparent(Color.BLACK, 70)));
		splitPanel.setLeftComponent(scrollPane_Left);
		// Right split panel-------------------------------------------------------------------------------
		scrollPane_Right = new JScrollPane();
		scrollPane_Right.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ColorUtil.makeTransparent(Color.BLACK, 70)));
		splitPanel.setRightComponent(scrollPane_Right);
		
		this.add(splitPanel, BorderLayout.CENTER);
	}
}

class INPUT_PANE extends JLayeredPane {
	private List<JCheckBox> year_checkbox, category_checkbox;
	private JPanel year_panel, category_panel;
	private JScrollPane year_scroll, category_scroll;
	
	public INPUT_PANE() {
		// add all layers labels and CheckBoxes to identifiersPanel
		year_panel = new JPanel(new GridBagLayout());
		category_panel = new JPanel(new GridBagLayout());		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
	    c.weighty = 1;
    
		// year
	    String[] year = new String[] { "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025" };
	    year_checkbox = new ArrayList<JCheckBox>();
 		for (int i = 0; i < year.length; i++) {
 			year_checkbox.add(new JCheckBox(year[i]));
 			c.gridx = 0;
 			c.gridy = i;
 			year_panel.add(year_checkbox.get(i), c);
 		}
	 		
		// category
 		String[] category = new String[] { "A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3", "C4", "D1", "D2" };
		String[] category_description = new String[] { 
				"Evacuations", "Road, Highway or Freeway Closures", "Extreme Fire Behavior, Weather Event, Natural or Human Caused Disasters", 
				"Structures (residential, commercial, vacation or other)", "Community Loss (within 48 hours)", "Infrastructure – National, State, and Local (Power Lines, Energy Corridors, Domestic Water Systems, Communication Grid, Transportation Systems, etc.)", 
				"Historical and Significant Cultural Resources", "Natural Resources (T&E Species Hab., Watershed, Forest Health, Soils, Air Shed, etc.)", "Commercial Resources (Grazing, Timber, Agricultural Crops, etc.)", "Potential for Economic Impact (Tourism i.e., fishing, hunting; loss of jobs, etc.)", 
				"Complex vs. Single Incident", "Potential for Timely Containment and/ or Mitigation" };
		category_checkbox = new ArrayList<JCheckBox>();
		for (int i = 0; i < category.length; i++) {
			category_checkbox.add(new JCheckBox(category[i]));
			category_checkbox.get(i).setToolTipText(category_description[i]);
			c.gridx = 1;
			c.gridy = i;
			category_panel.add(category_checkbox.get(i), c);
		}
	    
		// 2 scrolls
		year_scroll = new JScrollPane();
		year_scroll.setViewportView(year_panel);
		year_scroll.setViewportBorder(null);
		TitledBorder border = new TitledBorder("YEAR");
		border.setTitleJustification(TitledBorder.CENTER);
		year_scroll.setBorder(border);
		year_scroll.setPreferredSize(new Dimension(100, 400));
		
		category_scroll = new JScrollPane();
		category_scroll.setViewportView(category_panel);
		category_scroll.setViewportBorder(null);
		border = new TitledBorder("CATEGORY");
		border.setTitleJustification(TitledBorder.CENTER);
		category_scroll.setBorder(border);
		category_scroll.setPreferredSize(new Dimension(100, 400));
		
		// Button
		BUTTON_CALCULATE button_calculate = new BUTTON_CALCULATE();
		button_calculate.addActionListener(e -> {
			new Calculate_Final_Ranking(get_selected_years(), get_selected_categories());
		});	
		
		// Add to main pane
		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
	    c.weighty = 0;
	    c.gridwidth = 1;
		c.gridheight = 1;
		
		c.gridx = 0;
		c.gridy = 0;
		this.add(year_scroll, c);
		
		c.gridx = 1;
		c.gridy = 0;
		this.add(category_scroll, c);
		
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 1;
		this.add(button_calculate, c);
		
		c.weightx = 0;
	    c.weighty = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 2;
		this.add(new JLayeredPane(), c); // just too fill the lower gap
	}
	
	public List<String> get_selected_years() {
		List<String> selected_years = new ArrayList<String>();
		for (JCheckBox i : year_checkbox) {
			if (i.isSelected()) {
				selected_years.add(i.getText());
			}
		}
		return selected_years;
	}
	
	public List<String> get_selected_categories() {
		List<String> get_selected_categories = new ArrayList<String>();
		for (JCheckBox i : category_checkbox) {
			if (i.isSelected()) {
				get_selected_categories.add(i.getText());
			}
		}
		return get_selected_categories;
	}
	
	
	class BUTTON_CALCULATE extends JButton {
		public BUTTON_CALCULATE() {
//			button_calculate.setDisabledIcon(IconHandle.get_scaledImageIcon(128, 128, "icon_main.png"));
//			button_calculate.setDisabledIcon(IconHandle.get_scaledImageIcon_replicate(128, 128, "main_animation.gif"));
			setBackground(Color.WHITE);
			setHorizontalTextPosition(JButton.CENTER);
			setVerticalTextPosition(JButton.BOTTOM);
			setFont(new Font(null, Font.BOLD, 15));
			setText("CALCULATE");
			setRequestFocusEnabled(false);
		}
	}
}
