
package com.filemanager.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class FileContextMenu {

    private final PropertiesDialog propertiesDialog =
        new PropertiesDialog();

    public void attach(TreeView<Path> tree) {

        tree.setCellFactory(view -> new TreeCell<Path>() {

            @Override
            protected void updateItem(Path path, boolean empty) {
                super.updateItem(path, empty);

                if (empty || path == null) {
                    setText(null);
                    setContextMenu(null);
                    return;
                }

                setText(path.getFileName() == null
                    ? path.toString()
                    : path.getFileName().toString());

                ContextMenu menu = createMenu(tree, path);

setContextMenu(menu);

setOnContextMenuRequested(event -> {
    if (!isEmpty()) {
        tree.getSelectionModel().select(getTreeItem());
    }
});
            }
        });
    }

    private ContextMenu createMenu(
            TreeView<Path> tree, Path path) {

        ContextMenu menu = new ContextMenu();

        MenuItem open = new MenuItem("Open");
        MenuItem rename = new MenuItem("Rename");
        MenuItem copy = new MenuItem("Copy");
        MenuItem move = new MenuItem("Move");
        MenuItem delete = new MenuItem("Delete");
        MenuItem properties = new MenuItem("Properties");

        open.setOnAction(event -> {
            if (Files.isDirectory(path)) {
                tree.getSelectionModel().select(
                    tree.getRow(
                        findTreeItem(tree.getRoot(), path)
                    )
                );
            } else {
                showInfo(
                    "Open",
                    "Connect this action to your file-opening service."
                );
            }
        });

        rename.setOnAction(event -> renamePath(tree, path));

        copy.setOnAction(event -> copyPath(path));

        move.setOnAction(event -> movePath(path));

        delete.setOnAction(event -> deletePath(tree, path));

        properties.setOnAction(event ->
            propertiesDialog.show(path, tree.getScene().getWindow())
        );

        menu.getItems().addAll(
            open,
            rename,
            copy,
            move,
            new SeparatorMenuItem(),
            delete,
            new SeparatorMenuItem(),
            properties
        );

        return menu;
    }

    private void renamePath(TreeView<Path> tree, Path path) {

        if (path.getFileName() == null) {
            showError("Cannot rename the filesystem root.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(
            path.getFileName().toString()
        );

        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename file or folder");
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()
                    || name.equals(".")
                    || name.equals("..")
                    || name.contains("/")
                    || name.contains("\\")) {
                showError("Enter a valid file or folder name.");
                return;
            }

            try {
                Path target = path.resolveSibling(name);

                if (Files.exists(target)) {
                    showError("An item with that name already exists.");
                    return;
                }

                Files.move(path, target);
                showInfo("Rename", "Item renamed successfully.");

                // Refresh the explorer through your team's file-view
                // refresh method when integrating the full application.

            } catch (IOException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void copyPath(Path path) {
        showInfo(
            "Copy",
            "Selected item: " + path
                + "\nConnect this action to your copy workflow."
        );
    }

    private void movePath(Path path) {
        showInfo(
            "Move",
            "Selected item: " + path
                + "\nConnect this action to your destination-folder workflow."
        );
    }

    private void deletePath(TreeView<Path> tree, Path path) {

        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Delete " + path.getFileName() + "?"
        );

        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("This operation may be irreversible.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            // Only deletes an empty directory or a regular file.
            // Non-empty directories require an explicit recursive policy.
            Files.delete(path);

            showInfo("Delete", "Item deleted successfully.");

            // Refresh the explorer through your team's file-view
            // refresh method when integrating the full application.

        } catch (IOException ex) {
            showError(
                "Could not delete item. It may be a non-empty folder.\n"
                    + ex.getMessage()
            );
        }
    }

    private javafx.scene.control.TreeItem<Path> findTreeItem(
            javafx.scene.control.TreeItem<Path> item, Path path) {

        if (item == null) {
            return null;
        }

        if (path.equals(item.getValue())) {
            return item;
        }

        for (var child : item.getChildren()) {
            var result = findTreeItem(child, path);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File Operation Error");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
