package convenience_classes;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import root.IMSRmain;

public class IconHandle {
	public IconHandle() {

	}

	public static ImageIcon get_scaledImageIcon(int width, int height, String imageName) {
		try {
			ImageIcon icon = new ImageIcon(IMSRmain.get_DesktopPane().getClass().getResource("/" + imageName));
			Image scaleImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
			ImageIcon scaledImageIcon = new ImageIcon(scaleImage);				
			return scaledImageIcon;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static ImageIcon get_scaledImageIcon_replicate(int width, int height, String imageName) {
		try {
			ImageIcon icon = new ImageIcon(IMSRmain.get_DesktopPane().getClass().getResource("/" + imageName));
			Image scaleImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_REPLICATE);
			ImageIcon scaledImageIcon = new ImageIcon(scaleImage);				
			return scaledImageIcon;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static ImageIcon get_rotated_scaledImageIcon(int width, int height, String imageName) {
		try {
			ImageIcon icon = new ImageIcon(IMSRmain.get_DesktopPane().getClass().getResource("/" + imageName));
			BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.createGraphics();
			// paint the Icon to the BufferedImage.
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
		    BufferedImage rotated = rotate(bi, 180.0d);
		    Image scaleImage = rotated.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			ImageIcon scaledImageIcon = new ImageIcon(scaleImage);
			return scaledImageIcon;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static BufferedImage rotate(BufferedImage image, Double degrees) {
	    // Calculate the new size of the image based on the angle of rotaion
	    double radians = Math.toRadians(degrees);
	    double sin = Math.abs(Math.sin(radians));
	    double cos = Math.abs(Math.cos(radians));
	    int newWidth = (int) Math.round(image.getWidth() * cos + image.getHeight() * sin);
	    int newHeight = (int) Math.round(image.getWidth() * sin + image.getHeight() * cos);

	    // Create a new image
	    BufferedImage rotate = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = rotate.createGraphics();
	    // Calculate the "anchor" point around which the image will be rotated
	    int x = (newWidth - image.getWidth()) / 2;
	    int y = (newHeight - image.getHeight()) / 2;
	    // Transform the origin point around the anchor point
	    AffineTransform at = new AffineTransform();
	    at.setToRotation(radians, x + (image.getWidth() / 2), y + (image.getHeight() / 2));
	    at.translate(x, y);
	    g2d.setTransform(at);
	    // Paint the originl image
	    g2d.drawImage(image, 0, 0, null);
	    g2d.dispose();
	    return rotate;
	}
}