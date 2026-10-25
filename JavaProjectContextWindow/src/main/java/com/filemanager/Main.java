

package com.filemanager;

import com.filemanager.ui.FileContextMenu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Path rootPath = Path.of(".").toAbsolutePath().normalize();

        TreeItem<Path> root = new TreeItem<>(rootPath);
        root.setExpanded(true);

        try (var stream = Files.list(rootPath)) {
            stream.forEach(path -> root.getChildren().add(
                new TreeItem<>(path)
            ));
        }

        TreeView<Path> tree = new TreeView<>(root);
        tree.setShowRoot(true);

        FileContextMenu contextMenu = new FileContextMenu();
        contextMenu.attach(tree);

        stage.setTitle("File Manager Context Menu Test");
        stage.setScene(new Scene(tree, 700, 500));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
