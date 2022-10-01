package filesynchelper.service;

import filesynchelper.entity.CompareTreeNode;
import filesynchelper.entity.FileTreeNode;
import filesynchelper.view.FilePanel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guo
 */
@Slf4j
public class Controller {
    private String souRootPath;
    private String desRootPath;
    private FileTreeNode souTreeRoot;
    private FileTreeNode desTreeRoot;
    private CompareTreeNode compareTreeNode;
    private List<String> nameList = new ArrayList<>();

    public void set(String souRootPath, String desRootPath) {
        this.souRootPath = souRootPath;
        this.desRootPath = desRootPath;
        souTreeRoot = null;
        desTreeRoot = null;
        compareTreeNode = null;
        nameList.clear();

        souTreeRoot = Service.loadFileTree(new File(souRootPath));
        desTreeRoot = Service.loadFileTree(new File(desRootPath));
        compare();
    }

    public void update() {
        File souRootFile = new File(souRootPath);
        File desRootFile = new File(desRootPath);
        souTreeRoot = Service.updateFileTree(nameList, 0, souRootFile.getName(), souTreeRoot, souRootFile);
        desTreeRoot = Service.updateFileTree(nameList, 0, desRootFile.getName(), desTreeRoot, desRootFile);
        compare();
    }

    public void compare() {
        FileTreeNode souTree = Service.findFileTreeNode(nameList, souTreeRoot);
        FileTreeNode desTree = Service.findFileTreeNode(nameList, desTreeRoot);
//        log.info(String.valueOf(souTree));
//        log.info(String.valueOf(desTree));
        compareTreeNode = Service.compareFileTree(souTree, desTree);
    }

    public void pushName(String name) {
        nameList.add(name);
        compare();
    }

    public String popName() {
        if (nameList.size() > 0) {
            String name = nameList.remove(nameList.size() - 1);
            compare();
            return name;
        } else {
            return null;
        }
    }

    public String getNamesString() {
        StringBuilder sb = new StringBuilder("\\");
        nameList.forEach(s -> sb.append(s + "\\"));
        return sb.toString();
    }

    public List<FilePanel.ButtonData> getButtonData() {
        List<FilePanel.ButtonData> result = new ArrayList<>();
        if (compareTreeNode != null) {
            for (CompareTreeNode childNode : compareTreeNode.getChildren()) {
                FilePanel.ButtonData buttonData = new FilePanel.ButtonData(childNode.getName(), childNode.getType(), childNode.getStatus());
                result.add(buttonData);
            }
        }
        return result;
    }
}
