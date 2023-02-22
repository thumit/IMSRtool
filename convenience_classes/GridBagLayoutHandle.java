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
import java.awt.Insets;

public class GridBagLayoutHandle {
	public static GridBagConstraints get_c(GridBagConstraints c, String c_fill, int gridx, int gridy, int gridwidth,
			int gridheight, double weightx, double weighty, int insets_top, int insets_left, int insets_bottom,
			int insets_right) {

		if (c_fill.equals("NONE")) {
			c.fill = GridBagConstraints.NONE;
		} else if (c_fill.equals("HORIZONTAL")) {
			c.fill = GridBagConstraints.HORIZONTAL;
		} else if (c_fill.equals("VERTICAL")) {
			c.fill = GridBagConstraints.VERTICAL;
		} else if (c_fill.equals("BOTH")) {
			c.fill = GridBagConstraints.BOTH;
		} else if (c_fill.equals("CENTER")) {
			c.fill = GridBagConstraints.CENTER;
		}

		c.gridx = gridx;
		c.gridy = gridy;
		c.gridwidth = gridwidth;
		c.gridheight = gridheight;
		c.weightx = weightx;
		c.weighty = weighty;
		c.insets = new Insets(insets_top, insets_left, insets_bottom, insets_right);
		return c;
	}
}
