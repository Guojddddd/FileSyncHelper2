package filesynchelper.entity;

import lombok.Data;

import java.util.*;

/**
 * @author guo
 */
@Data
public class CompareTreeNode implements Comparable<CompareTreeNode> {
	private String name;
	private FileType type;
	private EnumSet<CompareStatus> status;
	private List<CompareTreeNode> children;
	private long souSize;
	private long desSize;

	public CompareTreeNode(String name, FileType type, long souSize, long desSize) {
		this.name = name;
		this.type = type;
		this.status = EnumSet.noneOf(CompareStatus.class);
		this.children = new ArrayList<>();
		this.souSize = souSize;
		this.desSize = desSize;
	}

	public void addChild(CompareTreeNode child) {
		this.children.add(child);
		this.status.addAll(child.getStatus());
	}

	@Override
	public String toString() {
		return "CompareTreeNode{" +
				"name='" + name + '\'' +
				", type=" + type +
				", status=" + status +
				", children=" + children +
				'}';
	}

	@Override
	public int compareTo(CompareTreeNode o) {
		if (this.type != o.type) {
			return o.type.getCode() - this.type.getCode();
		} else {
			int thisMax = 0, oMax = 0;
			for (CompareStatus status : this.status) {
				thisMax = Math.max(thisMax, status.getCode());
			}
			for (CompareStatus status : o.status) {
				oMax = Math.max(oMax, status.getCode());
			}

			if (thisMax != oMax) {
				return oMax - thisMax;
			} else {
				return this.getName().compareTo(o.getName());
			}
		}
	}
}
