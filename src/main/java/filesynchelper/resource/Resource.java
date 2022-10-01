package filesynchelper.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guo
 */
@Slf4j
public class Resource {
	private static Map<String, ImageIcon> imageIconMap = new HashMap<>(8);

	static {
		try {
			InputStream is;
			is = new ClassPathResource("icon/dirIcon.png").getInputStream();
			imageIconMap.put("dirIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/fileIcon.png").getInputStream();
			imageIconMap.put("fileIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/imageIcon.png").getInputStream();
			imageIconMap.put("imageIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/videoIcon.png").getInputStream();
			imageIconMap.put("videoIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/voiceIcon.png").getInputStream();
			imageIconMap.put("voiceIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/packIcon.png").getInputStream();
			imageIconMap.put("packIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/programIcon.png").getInputStream();
			imageIconMap.put("programIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			is = new ClassPathResource("icon/docIcon.png").getInputStream();
			imageIconMap.put("docIcon", new ImageIcon(ImageIO.read(is)));
			is.close();

			log.info("读取资源完成");
		} catch (IOException e) {
			log.error("读取资源报错", e);
		}
	}

	public static ImageIcon getImageIconByName(String name) {
		return imageIconMap.get(name);
	}
}
