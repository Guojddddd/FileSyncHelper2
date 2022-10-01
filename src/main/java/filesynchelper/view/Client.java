package filesynchelper.view;

import filesynchelper.service.Controller;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

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
				scrollBarValues.clear();

				controller.set(souRootPathField.getText(), desRootPathField.getText());
				List<FilePanel.ButtonData> buttonDataList = controller.getButtonData();
				filePanel.setFileData(buttonDataList);
				setPathBox();
			});
		}
		{
			JButton updateButton = new JButton("UPDATE");
			updateButton.setSize(100, 70);
			updateButton.setLocation(690, 10);
			updateButton.setFocusPainted(false);
			operatePanel.add(updateButton);
			updateButton.addActionListener(e -> {
				int scrollValue = filePanel.getVerticalScrollBarValue();

				controller.update();
				List<FilePanel.ButtonData> buttonDataList = controller.getButtonData();
				filePanel.setFileData(buttonDataList);
				setPathBox();

				filePanel.setVerticalScrollBarValue(scrollValue);
			});
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

	private void setPathBox() {
		String originValue = controller.getNamesString();

		if (originValue.length() >= 50) {
			originValue = "..." + originValue.substring(originValue.length() - 47, originValue.length());
		}

		pathBox.setText(originValue);
	}
}
