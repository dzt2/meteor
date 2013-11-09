package lab.meteor.visualize.resource;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import lab.meteor.visualize.Launcher;
import co.gongzh.snail.Image;
import co.gongzh.snail.Image.ImageSourceLoader;
import co.gongzh.snail.ResizableImage;

public class Resources {
	
	public static final ResizableImage IMG_BLOCK_SHADOW;
	public static final Image IMG_RESIZE;

	public static final Color COLOR_CLASS_TITLE_BG;
	public static final Color COLOR_ENUM_TITLE_BG;
	public static final Color COLOR_PACKAGE_BG;
	public static final Color COLOR_CLASS_BORDER;
	public static final Color COLOR_ENUM_BORDER;
	public static final Color COLOR_PACKAGE_BORDER;
	
	public static final Color COLOR_PROPERTY_NAME;
	public static final Color COLOR_PROPERTY_HIGHLIGHT;
	public static final Color COLOR_PRIMITIVE_TYPE;
	public static final Color COLOR_ENUM_TYPE;
	public static final Color COLOR_CLASS_TYPE;
	
	public static final Color COLOR_CLASS_NAME;
	public static final Color COLOR_ENUM_NAME;
	
	public static final Color COLOR_COMMAND_BG;
	
	public static final Font FONT_PROPERTY;
	public static final Font FONT_CLASS_ENUM;
	public static final Font FONT_COMMAND;
	public static final Font FONT_CMD_PRINT;
	
	public static final Stroke STROKE_LINES;
	
	static {
		IMG_BLOCK_SHADOW = new ResizableImage(Launcher.getFrame().getGraphicsConfiguration(), new ImageLoader("shadow.png"), 3, 4, 190, 132);
		IMG_RESIZE = new Image(Launcher.getFrame().getGraphicsConfiguration(), new ImageLoader("resize.png"));
		
		COLOR_CLASS_TITLE_BG = new Color(0xffbfdad9);
		COLOR_CLASS_BORDER = new Color(0xff658a86);
		COLOR_ENUM_TITLE_BG = new Color(0xffd3e776);
		COLOR_ENUM_BORDER = new Color(0xff94ac36);
		COLOR_PACKAGE_BG = new Color(0xffeae392);
		COLOR_PACKAGE_BORDER = new Color(0xffa29a3c);
		
		COLOR_PROPERTY_NAME = Color.black;
		COLOR_PROPERTY_HIGHLIGHT = new Color(220, 220, 220);
		COLOR_PRIMITIVE_TYPE = new Color(195, 36, 133);
		COLOR_ENUM_TYPE = new Color(104, 133, 1);
		COLOR_CLASS_TYPE = new Color(69, 104, 101);
		
		COLOR_CLASS_NAME = new Color(26, 69, 77);
		COLOR_ENUM_NAME = new Color(94, 108, 34);
		
		COLOR_COMMAND_BG = new Color(0, 0, 0, 200);
		
		FONT_PROPERTY = new Font("Consolas", Font.PLAIN, 12);
		FONT_CLASS_ENUM = new Font("Corbel", Font.PLAIN, 14);
		FONT_COMMAND = new Font("Andale Mono", Font.PLAIN, 14);
		FONT_CMD_PRINT = new Font("Andale Mono", Font.PLAIN, 12);
		
		STROKE_LINES = new BasicStroke(2);
	}
	
	public static class ImageLoader implements ImageSourceLoader {

		final String name;
		public ImageLoader(String name) {
			this.name = name;
		}
		
		@Override
		public BufferedImage loadImage() {
			InputStream stream = Resources.class.getResourceAsStream(name);
			try {
				return ImageIO.read(stream);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		public void unloadImage(BufferedImage image) {
			image.flush();
		}
		
	}
}
