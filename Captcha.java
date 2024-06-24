import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;
public class Captcha{
	// 随机字符串的字符集
	private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int length=6,width=160,height=40;
	private static final Font font=new Font("黑体", Font.PLAIN, 24);
	private static SecureRandom random=new SecureRandom();
	String s;
	BufferedImage image;
	private static String generateString(){
		StringBuilder s = new StringBuilder(length);
		for(int i=0;i<length;i++){
			final int index=random.nextInt(alphabet.length());
			s.append(alphabet.charAt(index));
		}
        return s.toString();
    }
	private static BufferedImage generateImage(String s) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setFont(font);
		// 画出随机字符并进行扭曲
		for(int i=0;i<length;i++) {
			AffineTransform originalTransform = g.getTransform();
			// 生成随机扭曲
			double rotation=(random.nextDouble()-0.5) * Math.PI/6; // 旋转角度
			double shearX=(random.nextDouble()-0.5) * 0.3; // 水平扭曲
			double shearY=(random.nextDouble()-0.5) * 0.3; // 垂直扭曲
			AffineTransform transform = new AffineTransform();
			transform.rotate(rotation,25*i+10,20);
			transform.shear(shearX, shearY);
			g.setTransform(transform);
			// 设置随机颜色
			g.setColor(new Color(random.nextInt(255),
				random.nextInt(255),random.nextInt(255)));
			g.drawString(String.valueOf(s.charAt(i)),25*i+10,30);
			// 恢复原始变换
			g.setTransform(originalTransform);
		}
		// 添加一些干扰线
		for(int i=0;i<5;++i){
			g.setColor(new Color(random.nextInt(255),
				random.nextInt(255),random.nextInt(255)));
			final int x1 = random.nextInt(width);
			final int y1 = random.nextInt(height);
			final int x2 = random.nextInt(width);
			final int y2 = random.nextInt(height);
			g.drawLine(x1, y1, x2, y2);
		}
		g.dispose();
		return image;
	}
	Captcha(){
		s=generateString();
		image=generateImage(s);
	}
	private static void saveCaptchaImage(BufferedImage image, String filePath) {
		File outputfile = new File(filePath);
		try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			System.out.println("Error saving captcha image: " + e.getMessage());
		}
	}
}