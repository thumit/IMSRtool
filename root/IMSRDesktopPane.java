package root;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDesktopPane;


@SuppressWarnings("serial")
public class IMSRDesktopPane extends JDesktopPane {
	private BufferedImage img;
	
	public IMSRDesktopPane() {
		process_image();
	}
	
	public void process_image() {
		try {
//			img = ImageIO.read(new URL("https://scontent-iad3-1.xx.fbcdn.net/t31.0-8/705097_4557689934155_1784248166_o.jpg"));			   
//			img = ImageIO.read(new File("C:\\Users\\Public\\Pictures\\Sample Pictures\\Desert.jpg"));
			img = ImageIO.read(getClass().getResource("/IMSR.png"));
			BufferedImage bg = img;

			// Rescale buffered image-----------------
			final float FACTOR  = 4f;
			int scaleX = (int) (bg.getWidth() * FACTOR);
			int scaleY = (int) (bg.getHeight() * FACTOR);
			Image scaleImage = bg.getScaledInstance(200, 40, Image.SCALE_SMOOTH);
			BufferedImage bg2 = new BufferedImage(scaleX, scaleY, BufferedImage.TYPE_INT_ARGB);
			bg2.getGraphics().drawImage(scaleImage, 0, 0 , null);
			
//			setBackgroundImage(bg);
			setBackgroundImage(bg2);
		} catch (IOException ex) {
			System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
		}
	}

	@Override
	public Dimension getPreferredSize() {
		BufferedImage img = getBackgroundImage();
		Dimension size = super.getPreferredSize();
		if (img != null) {
			size.width = Math.max(size.width, img.getWidth());
			size.height = Math.max(size.height, img.getHeight());
		}
		return size;
	}
	
	public BufferedImage getBackgroundImage() {
		return img;
	}
	
	public void setBackgroundImage(BufferedImage value) {
		if (img != value) {
			BufferedImage old = img;
			img = value;
			firePropertyChange("background", old, img);
			revalidate();
			repaint();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		BufferedImage bg = getBackgroundImage();
		if (bg != null) {
			int x = (int) getWidth() - 205;
			int y = (int) getHeight() - 50;
			g.drawImage(bg, x, y, this);
		}
	}
}
