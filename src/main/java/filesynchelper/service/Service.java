package filesynchelper.service;

import filesynchelper.entity.CompareStatus;
import filesynchelper.entity.CompareTreeNode;
import filesynchelper.entity.FileTreeNode;
import filesynchelper.entity.FileType;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;

/**
 * @author guo
 */
@Slf4j
public class Service {
	public static FileTreeNode loadFileTree(File root) {
		if (root == null || !root.exists()) {
			return null;
		}

		FileTreeNode result;
		if (root.isFile()) {
			result = new FileTreeNode(FileType.FILE, root.getName());
			result.setSize(root.length());
			result.setFileCount(1);
		} else {
			result = new FileTreeNode(FileType.DIR, root.getName());
			File[] children = root.listFiles();

			for (File child : children) {
				FileTreeNode childNode = loadFileTree(child);
				if (childNode != null) {
					result.addChild(childNode);
				}
			}
		}
		return result;
	}

	public static FileTreeNode updateFileTree(List<String> childNameList, int childNameIndex, String parentName, FileTreeNode parentNode, File parentFile) {
		if (parentFile == null || !parentFile.exists()) {
			return null;
		}
		if (childNameList.size() == childNameIndex) {
			return loadFileTree(parentFile);
		} else {
			String childName = childNameList.get(childNameIndex);
			FileTreeNode childNode = updateFileTree(childNameList, childNameIndex + 1, childName, parentNode != null ? parentNode.getChildren().get(childName) : null, new File(parentFile, childName));

			FileTreeNode parentNew = new FileTreeNode(FileType.DIR, parentName);
			Map<String, FileTreeNode> children = parentNode != null ? parentNode.getChildren() : new HashMap<>(8);
			if (childNode != null) {
				children.put(childName, childNode);
			} else {
				children.remove(childName);
			}

			for (FileTreeNode child : children.values()) {
				parentNew.addChild(child);
			}

			return parentNew;
		}
	}

	public static FileTreeNode findFileTreeNode(List<String> nameList, FileTreeNode rootNode) {
		for (String name : nameList) {
			if (rootNode != null) {
				rootNode = rootNode.getChildren().get(name);
			}
		}
		return rootNode;
	}

	public static CompareTreeNode compareFileTree(FileTreeNode souTree, FileTreeNode desTree) {
		if (souTree == null && desTree == null) {
			return null;
		} else if (souTree != null && desTree == null) {
			CompareTreeNode result = new CompareTreeNode(souTree.getName(), souTree.getType());
			if (souTree.getType() == FileType.FILE) {
				result.getStatus().add(CompareStatus.STATUS_LOST);
			} else {
				for (FileTreeNode childNode : souTree.getChildren().values()) {
					CompareTreeNode childCompareNode = compareFileTree(childNode, null);
					result.addChild(childCompareNode);
				}
				result.getStatus().add(CompareStatus.STATUS_LOST);
			}
			Collections.sort(result.getChildren());
			return result;
		} else if (souTree == null && desTree != null) {
			CompareTreeNode result = new CompareTreeNode(desTree.getName(), desTree.getType());
			if (desTree.getType() == FileType.FILE) {
				result.getStatus().add(CompareStatus.STATUS_OVER);
			} else {
				for (FileTreeNode childNode : desTree.getChildren().values()) {
					CompareTreeNode childCompareNode = compareFileTree(null, childNode);
					result.addChild(childCompareNode);
				}
				result.getStatus().add(CompareStatus.STATUS_OVER);
			}
			Collections.sort(result.getChildren());
			return result;
		} else {
			CompareTreeNode result;
			if (souTree.getType() != desTree.getType()) {
				result = new CompareTreeNode(souTree.getName(), FileType.FILE);
				result.getStatus().add(CompareStatus.STATUS_DIFF_TYPE);
				return result;
			} else {
				result = new CompareTreeNode(souTree.getName(), souTree.getType());
				if (souTree.getType() == FileType.FILE) {
					if (souTree.getSize() == desTree.getSize()) {
						result.getStatus().add(CompareStatus.STATUS_SAME);
					} else {
						result.getStatus().add(CompareStatus.STATUS_DIFF_SIZE);
					}
				} else {
					Set<String> names = new HashSet<>();
					names.addAll(souTree.getChildren().keySet());
					names.addAll(desTree.getChildren().keySet());

					for (String name : names) {
						CompareTreeNode childCompareNode = compareFileTree(souTree.getChildren().get(name), desTree.getChildren().get(name));
						result.addChild(childCompareNode);
					}
				}

				if (result.getStatus().size() == 0) {
					result.getStatus().add(CompareStatus.STATUS_SAME);
				}
			}
			Collections.sort(result.getChildren());
			return result;
		}
	}
}
