package filesynchelper.view;

import filesynchelper.service.Controller;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

/**
 * @author guo
 */
@Slf4j
public class Client extends JFrame {
	public static void main(String[] args) {
		new Client();
	}

	public Client() {
		this.setSize(900, 600);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLocation(100, 100);
		this.setTitle("文件同步辅助器");
		this.init();
		this.setVisible(true);
	}

	private JTextField souRootPathField = new JTextField();
	private JTextField desRootPathField = new JTextField();
	private Controller controller = new Controller();
	private FilePanel filePanel = new FilePanel();
	private JLabel bottomShow = new JLabel("-");
	private JLabel pathBox = new JLabel();
	private Deque<Integer> scrollBarValues = new ArrayDeque<>();

	private void init() {
		JPanel operatePanel = new JPanel();
		operatePanel.setLayout(null);
		operatePanel.setSize(900, 130);
		operatePanel.setPreferredSize(new Dimension(900, 130));
		{
			souRootPathField.setLocation(70, 10);
			souRootPathField.setSize(500, 30);
			desRootPathField.setLocation(70, 50);
			desRootPathField.setSize(500, 30);
			operatePanel.add(souRootPathField);
			operatePanel.add(desRootPathField);
			operatePanel.add(getLabel("sou path", 50, 30, 10, 10));
			operatePanel.add(getLabel("des path", 50, 30, 10, 50));
		}
		{
			JButton setButton = new JButton("SET");
			setButton.setSize(100, 70);
			setButton.setLocation(580, 10);
			setButton.setFocusPainted(false);
			operatePanel.add(setButton);
			setButton.addActionListener(e -> {
				int option = JOptionPane.showConfirmDialog(this, "读取需要一定时间，确认设置？", "确认", JOptionPane.YES_NO_OPTION);

				if (option == JOptionPane.YES_OPTION) {
					scrollBarValues.clear();

					controller.set(souRootPathField.getText(), desRootPathField.getText());
					List<FilePanel.ButtonData> buttonDataList = controller.getButtonData();
					filePanel.setFileData(buttonDataList);
					setPathBox();
				}
			});
		}
		{
			JButton updateButton = new JButton("UPDATE");
			updateButton.setSize(100, 30);
			updateButton.setLocation(690, 10);
			updateButton.setFocusPainted(false);
			operatePanel.add(updateButton);
			updateButton.addActionListener(e -> {
				update();
			});
		}
		{
			JButton syncButton = new JButton("SYNC");
			syncButton.setSize(100, 30);
			syncButton.setLocation(690, 50);
			syncButton.setFocusPainted(false);
			operatePanel.add(syncButton);
			syncButton.addActionListener(e -> {
				int option = JOptionPane.showConfirmDialog(this, "确认同步？", "确认", JOptionPane.YES_NO_OPTION);

				if (option == JOptionPane.YES_OPTION) {
					Function<String, Object> updateFunction = s -> {
						JOptionPane.showMessageDialog(this, s, "同步结束", JOptionPane.INFORMATION_MESSAGE);
						syncButton.setEnabled(true);

						update();

						return null;
					};
					syncButton.setEnabled(false);
					String syncResult = controller.syncCurrentDir(bottomShow, updateFunction);

					if (syncResult != null) {
						JOptionPane.showMessageDialog(this, syncResult);
						syncButton.setEnabled(true);
					}
				}
			});
		}
		{
			operatePanel.add(getColorPanel(15, 13, 800, 10, FilePanel.COLOR_SAME));
			operatePanel.add(getLabel("相同", 80, 13, 820, 10));
			operatePanel.add(getColorPanel(15, 13, 800, 24, FilePanel.COLOR_OVER));
			operatePanel.add(getLabel("多余", 80, 13, 820, 24));
			operatePanel.add(getColorPanel(15, 13, 800, 38, FilePanel.COLOR_DIFF_SIZE));
			operatePanel.add(getLabel("不同大小", 80, 13, 820, 38));
			operatePanel.add(getColorPanel(15, 13, 800, 52, FilePanel.COLOR_DIFF_TYPE));
			operatePanel.add(getLabel("不同类型", 80, 13, 820, 52));
			operatePanel.add(getColorPanel(15, 13, 800, 66, FilePanel.COLOR_LOST));
			operatePanel.add(getLabel("缺失", 80, 13, 820, 66));
		}
		{
			JButton backButton = new JButton("BACK");
			backButton.setSize(100, 30);
			backButton.setLocation(10, 90);
			backButton.setFocusPainted(false);
			operatePanel.add(backButton);
			backButton.addActionListener(e -> {
				if (controller.popName() != null) {
					List<FilePanel.ButtonData> buttonDataList = controller.getButtonData();
					filePanel.setFileData(buttonDataList);
					setPathBox();

					if (!scrollBarValues.isEmpty()) {
						filePanel.setVerticalScrollBarValue(scrollBarValues.pop());
					}
				}
			});
		}
		{
			pathBox.setSize(700, 30);
			pathBox.setLocation(130, 90);
			pathBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			pathBox.setHorizontalAlignment(SwingConstants.LEFT);
			pathBox.setFont(new Font("宋体", Font.PLAIN, 12));
			operatePanel.add(pathBox);
		}
		this.add(operatePanel, BorderLayout.NORTH);

		this.add(filePanel, BorderLayout.CENTER);

		this.add(bottomShow, BorderLayout.SOUTH);

		filePanel.setFileButtonCallBack(name -> {
			scrollBarValues.push(filePanel.getVerticalScrollBarValue());

			controller.pushName(name);
			List<FilePanel.ButtonData> buttonDataList = controller.getButtonData();
			filePanel.setFileData(buttonDataList);
			setPathBox();
		});
	}

	private JLabel getLabel(String msg, int w, int h, int x, int y) {
		JLabel label = new JLabel(msg);
		label.setSize(w, h);
		label.setLocation(x, y);
		return label;
	}

	private JPanel getColorPanel(int w, int h, int x, int y, Color color) {
		JPanel panel = new JPanel();
		panel.setSize(w, h);
		panel.setPreferredSize(new Dimension(w, h));
		panel.setLocation(x, y);
		panel.setBackground(color);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		return panel;
	}

	private void setPathBox() {
		String originValue = controller.getNamesString();

		if (originValue.length() >= 50) {
			originValue = "..." + originValue.substring(originValue.length() - 47, originValue.length());
		}

		pathBox.setText(originValue);
	}

	private void update() {
		int scrollValue = filePanel.getVerticalScrollBarValue();

		controller.update();
		List<FilePanel.ButtonData> buttonDataList = controller.getButtonData();
		filePanel.setFileData(buttonDataList);
		setPathBox();

		filePanel.setVerticalScrollBarValue(scrollValue);
	}
}
