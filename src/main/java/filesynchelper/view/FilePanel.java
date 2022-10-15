package filesynchelper.view;

import filesynchelper.entity.CompareStatus;
import filesynchelper.entity.FileType;
import filesynchelper.resource.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author guo
 */
@Slf4j
public class FilePanel extends JScrollPane {
	public static final Color COLOR_SAME      = new Color(170, 250, 170);
	public static final Color COLOR_OVER      = new Color(170, 230, 250);
	public static final Color COLOR_DIFF_SIZE = new Color(250, 250, 170);
	public static final Color COLOR_DIFF_TYPE = new Color(250, 170, 250);
	public static final Color COLOR_LOST      = new Color(250, 170, 170);

	@Data
	public static class ButtonData {
		private String name;
		private FileType type;
		private EnumSet<CompareStatus> status;
		private long souSize;
		private long desSize;

		public ButtonData(String name, FileType type, EnumSet<CompareStatus> status, long souSize, long desSize) {
			this.name = name;
			this.type = type;
			this.status = status;
			this.souSize = souSize;
			this.desSize = desSize;
		}

		@Override
		public String toString() {
			return "ButtonData{" +
					"name='" + name + '\'' +
					", type=" + type +
					", status=" + status +
					'}';
		}
	}

	public interface FileButtonCallBack {
		void callBack(String name);
	}

	private static class FileButton extends JPanel {
		private static final boolean DEBUG_BORDER = false;

		private static final Dimension SIZE = new Dimension(850, 25);
		private static final Font FONT = new Font("宋体", Font.PLAIN, 12);
		private static final Map<String, String> ICON_MAP = new HashMap<String, String>() {{
			put("jpg",  "imageIcon");
			put("jpeg", "imageIcon");
			put("png",  "imageIcon");
			put("gif",  "imageIcon");
			put("bmp",  "imageIcon");
			put("ico",  "imageIcon");

			put("avi", "videoIcon");
			put("mp4", "videoIcon");
			put("mkv", "videoIcon");
			put("wmv", "videoIcon");
			put("flv", "videoIcon");
			put("mov", "videoIcon");

			put("mp3", "voiceIcon");
			put("wav", "voiceIcon");
			put("wma", "voiceIcon");

			put("zip", "packIcon");
			put("rar", "packIcon");
			put("7z",  "packIcon");
			put("tar", "packIcon");
			put("gz",  "packIcon");

			put("exe", "programIcon");
			put("bat", "programIcon");
			put("lnk", "programIcon");
			put("sh",  "programIcon");

			put("txt",  "docIcon");
			put("pdf",  "docIcon");
			put("doc",  "docIcon");
			put("docx", "docIcon");
			put("ppt",  "docIcon");
			put("pptx", "docIcon");
			put("xls",  "docIcon");
			put("xlsx", "docIcon");
		}};

		private String name;

		public FileButton(ButtonData buttonData, FileButtonCallBack fileButtonCallBack) {
			JLabel sizeLabel;

			{
				JLabel nameLabel = new JLabel(buttonData.getName());
				nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
				nameLabel.setLocation(30, 2);
				nameLabel.setSize(670, 21);
				nameLabel.setFont(FONT);
				if (DEBUG_BORDER) {
					nameLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
				}
				this.add(nameLabel);
			}
			{
				JLabel iconLabel = new JLabel();
				if (buttonData.getType() == FileType.FILE) {
					String extension = String.valueOf(StringUtils.getFilenameExtension(buttonData.getName())).toLowerCase();
					iconLabel.setIcon(Resource.getImageIconByName(ICON_MAP.getOrDefault(extension, "fileIcon")));
				} else if (buttonData.getType() == FileType.DIR) {
					iconLabel.setIcon(Resource.getImageIconByName("dirIcon"));
				}
				iconLabel.setLocation(3, 3);
				iconLabel.setSize(iconLabel.getPreferredSize());
				if (DEBUG_BORDER) {
					iconLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
				}
				this.add(iconLabel);
			}
			{
				sizeLabel = new JLabel();
				sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				sizeLabel.setLocation(710, 2);
				sizeLabel.setSize(120, 21);
				sizeLabel.setFont(FONT);
				if (DEBUG_BORDER) {
					sizeLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
				}
				this.add(sizeLabel);
			}

			this.setLayout(null);
			this.name = buttonData.getName();
			this.setOpaque(true);
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			this.setBackground(Color.WHITE);
			this.setSize(SIZE);
			this.setPreferredSize(SIZE);
			this.setEnabled(false);

			if (buttonData.getType() == FileType.DIR) {
				this.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						FileButton.super.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						FileButton.super.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
					}

					@Override
					public void mousePressed(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON1 && fileButtonCallBack != null) {
							fileButtonCallBack.callBack(buttonData.getName());
						}
					}
				});
			}

			if (buttonData.getStatus().contains(CompareStatus.STATUS_LOST)) {
				sizeLabel.setText(getSizeString(buttonData.getSouSize()));
				this.setBackground(COLOR_LOST);
			} else if (buttonData.getStatus().contains(CompareStatus.STATUS_DIFF_TYPE)) {
				this.setBackground(COLOR_DIFF_TYPE);
			} else if (buttonData.getStatus().contains(CompareStatus.STATUS_DIFF_SIZE)) {
				sizeLabel.setText(getSizeString(buttonData.getSouSize()) + " -> " + getSizeString(buttonData.getDesSize()));
				this.setBackground(COLOR_DIFF_SIZE);
			} else if (buttonData.getStatus().contains(CompareStatus.STATUS_OVER)) {
				sizeLabel.setText(getSizeString(buttonData.getDesSize()));
				this.setBackground(COLOR_OVER);
			} else if (buttonData.getStatus().contains(CompareStatus.STATUS_SAME)) {
				sizeLabel.setText(getSizeString(buttonData.getSouSize()));
				this.setBackground(COLOR_SAME);
			}
		}

		private String getSizeString(long size) {
			NumberFormat numberFormat = new DecimalFormat("###0.00");

			if (size < 512L) {
				return size + "B";
			} else if (size < 512L * 1024) {
				return numberFormat.format(size / 1024.0) + "KB";
			} else if (size < 512L * 1024 * 1024) {
				return numberFormat.format(size / (1024.0 * 1024.0)) + "MB";
			} else {
				return numberFormat.format(size / (1024.0 * 1024.0 * 1024.0)) + "GB";
			}
		}
	}

	private FileButtonCallBack fileButtonCallBack;

	public FilePanel() {
		this.getVerticalScrollBar().setUnitIncrement(10);
	}

	public void setFileButtonCallBack(FileButtonCallBack fileButtonCallBack) {
		this.fileButtonCallBack = fileButtonCallBack;
	}

	public int getVerticalScrollBarValue() {
		return this.getVerticalScrollBar().getValue();
	}

	public void setVerticalScrollBarValue(int value) {
		this.getVerticalScrollBar().setValue(value);
	}

	public void setFileData(List<ButtonData> buttonDataList) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setPreferredSize(new Dimension(FileButton.SIZE.width, FileButton.SIZE.height * buttonDataList.size()));

		for (int i = 0; i < buttonDataList.size(); i ++) {
			ButtonData buttonData = buttonDataList.get(i);
			FileButton fileButton = new FileButton(buttonData, fileButtonCallBack);
			fileButton.setLocation(0, FileButton.SIZE.height * i);
			panel.add(fileButton);
		}

		this.setViewportView(panel);
		this.repaint();
	}
}
