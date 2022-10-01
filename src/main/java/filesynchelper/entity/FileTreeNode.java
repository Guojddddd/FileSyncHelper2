package filesynchelper.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guo
 */
@Data
public class FileTreeNode {
	private FileType type;
	private String name;
	private long size;
	private int fileCount;
	private Map<String, FileTreeNode> children;

	public FileTreeNode(FileType type, String name) {
		this.type = type;
		this.name = name;
		this.children = new HashMap<>();
	}

	public void addChild(FileTreeNode child) {
		this.children.put(child.getName(), child);
		this.size += child.getSize();
		this.fileCount += child.getFileCount();
	}

	@Override
	public String toString() {
		return "FileTreeNode{" +
				"type=" + type +
				", name='" + name + '\'' +
				", size=" + size +
				", fileCount=" + fileCount +
				", children=" + children +
				'}';
	}
}
