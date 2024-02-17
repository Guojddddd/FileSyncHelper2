package filesynchelper.service;

import filesynchelper.entity.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * @author guo
 */
@Slf4j
public class Service {
	private static byte[] buffer = new byte[10 * 1024 * 1024];

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

			if (children != null) {
				for (File child : children) {
					if (child.isHidden()) {
						continue;
					}

					FileTreeNode childNode = loadFileTree(child);
					if (childNode != null) {
						result.addChild(childNode);
					}
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
			CompareTreeNode result = new CompareTreeNode(souTree.getName(), souTree.getType(), souTree.getSize(), 0);
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
			CompareTreeNode result = new CompareTreeNode(desTree.getName(), desTree.getType(), 0, desTree.getSize());
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
				result = new CompareTreeNode(souTree.getName(), FileType.FILE, souTree.getSize(), desTree.getSize());
				result.getStatus().add(CompareStatus.STATUS_DIFF_TYPE);
				return result;
			} else {
				result = new CompareTreeNode(souTree.getName(), souTree.getType(), souTree.getSize(), desTree.getSize());
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

	/**
	 * 查找所有缺失的路径
	 * @param souPath
	 * @param desPath
	 * @param compareTreeNode
	 * @param result
	 */
	public static void getAllLostPath(StringBuilder souPath, StringBuilder desPath, CompareTreeNode compareTreeNode, List<Pair<String, String>> result) {
		if (compareTreeNode.getStatus().size() == 1 && compareTreeNode.getStatus().contains(CompareStatus.STATUS_LOST)) {
			result.add(new Pair<>(souPath.toString(), desPath.toString()));
		}

		if (compareTreeNode.getChildren() != null) {
			for (CompareTreeNode childNode : compareTreeNode.getChildren()) {
				if (childNode.getStatus().contains(CompareStatus.STATUS_LOST)) {
					souPath.append("\\");
					souPath.append(childNode.getName());
					desPath.append("\\");
					desPath.append(childNode.getName());

					getAllLostPath(souPath, desPath, childNode, result);

					souPath.delete(souPath.length() - childNode.getName().length() - 1, souPath.length());
					desPath.delete(desPath.length() - childNode.getName().length() - 1, desPath.length());
				}
			}
		}
	}

	/**
	 * 复制一个文件
	 * @param oneCopy
	 * @throws Exception
	 */
	public static void copyFile(Pair<String, String> oneCopy) throws Exception {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File inputFile = new File(oneCopy.getKey());
		File outputFile = new File(oneCopy.getValue());

//		log.info(oneCopy.getKey() + "," + oneCopy.getValue());

		if (!inputFile.exists()) {
			throw new Exception("输入文件不存在：" + oneCopy.getKey());
		}

		if (outputFile.exists()) {
			log.warn("输出文件已存在：" + oneCopy.getValue());
			return;
		}
		if (inputFile.isDirectory()) {
			// 是目录，直接创建即可
			outputFile.mkdirs();
			return;
		}

		try {
			fis = new FileInputStream(inputFile);
			fos = new FileOutputStream(outputFile);

			while (true) {
				int readSize =  fis.read(buffer);
				if (readSize == -1) {
					break;
				}

				fos.write(buffer, 0, readSize);
			}

			fis.close();
			fos.close();
		} catch (Exception exc) {
			log.error("复制文件报错", exc);

			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
}
