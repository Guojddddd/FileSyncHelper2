package filesynchelper.service;

import filesynchelper.entity.CompareTreeNode;
import filesynchelper.entity.FileTreeNode;
import filesynchelper.view.FilePanel;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

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
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), runnable -> new Thread(runnable, "Controller.class"));

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
        if (isSetted()) {
            File souRootFile = new File(souRootPath);
            File desRootFile = new File(desRootPath);
            souTreeRoot = Service.updateFileTree(nameList, 0, souRootFile.getName(), souTreeRoot, souRootFile);
            desTreeRoot = Service.updateFileTree(nameList, 0, desRootFile.getName(), desTreeRoot, desRootFile);
            compare();
        }
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
                FilePanel.ButtonData buttonData = new FilePanel.ButtonData(childNode.getName(), childNode.getType(), childNode.getStatus(), childNode.getSouSize(), childNode.getDesSize());
                result.add(buttonData);
            }
        }
        return result;
    }

    /**
     * ?????????????????????????????????
     * @return
     */
    private boolean isSetted() {
        return compareTreeNode != null;
    }

    /**
     * ?????????????????????
     * @param bottomShow ??????JLabel
     * @param finishCallBack ?????????????????????
     * @return ???????????????????????????null???????????????not null
     */
    public String syncCurrentDir(JLabel bottomShow, Function<String, Object> finishCallBack) {
        if (isSetted()) {
            StringBuilder souPath = new StringBuilder(souRootPath);
            StringBuilder desPath = new StringBuilder(desRootPath);
            List<Pair<String, String>> lostFiles = new ArrayList<>();

            for (String name : nameList) {
                souPath.append("\\");
                souPath.append(name);
                desPath.append("\\");
                desPath.append(name);
            }

            Service.getAllLostPath(souPath, desPath, compareTreeNode, lostFiles);

            lostFiles.sort((a, b) -> {
                String as = a.getValue();
                String bs = b.getValue();

                if (as.length() != bs.length()) {
                    return as.length() - bs.length();
                } else {
                    return as.compareTo(bs);
                }
            });

            // ????????????????????????????????????????????????????????????
            Runnable runnable = () -> {
                try {
                    for (int i = 0; i < lostFiles.size(); i ++) {
                        Pair<String, String> oneCopy = lostFiles.get(i);
                        bottomShow.setText("???????????????" + (i + 1) + "/" + lostFiles.size());
                        Service.copyFile(oneCopy);
                    }
                    finishCallBack.apply("????????????");
                } catch (Exception e) {
                    log.error("??????", e);
                    finishCallBack.apply("?????????");
                }
            };
            threadPoolExecutor.submit(runnable);
            return null;
        } else {
            return "????????????";
        }
    }
}
