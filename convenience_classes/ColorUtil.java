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

public class ColorUtil {
	
	public static Color makeTransparent(Color source, int alpha) {
		return new Color(source.getRed(), source.getGreen(), source.getBlue(), alpha);
	}

}
