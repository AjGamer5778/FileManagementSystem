
package com.filemanager.ui;

import com.filemanager.model.FileMetadata;
import com.filemanager.service.MetadataService;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class PropertiesDialog {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final MetadataService metadataService =
        new MetadataService();

    public void show(java.nio.file.Path path, Window owner) {

        FileMetadata metadata;

        try {
            metadata = metadataService.read(path);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Properties");
            alert.setHeaderText("Cannot read file properties");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Properties - " + metadata.getName());
        stage.initModality(Modality.WINDOW_MODAL);

        if (owner != null) {
            stage.initOwner(owner);
        }

        Label nameLabel = new Label(metadata.getName());
        nameLabel.setStyle(
            "-fx-font-size: 20px; -fx-font-weight: bold;"
        );

        Label typeLabel = new Label(metadata.getType());
        typeLabel.setStyle("-fx-text-fill: gray;");

        VBox heading = new VBox(5, nameLabel, typeLabel);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        int row = 0;

        addRow(grid, row++, "Name", metadata.getName());
        addRow(grid, row++, "Type", metadata.getType());

        addRow(grid, row++, "Extension",
            metadata.getExtension().isEmpty()
                ? "None"
                : "." + metadata.getExtension());

        addRow(grid, row++, "Full path",
            metadata.getPath().toString());

        addRow(grid, row++, "Location",
            metadata.getPath().getParent() == null
                ? metadata.getPath().toString()
                : metadata.getPath().getParent().toString());

        addRow(grid, row++, "Size",
            metadata.isDirectory()
                ? "Directory entry size: "
                    + metadata.getSize() + " bytes"
                : formatSize(metadata.getSize()));

        addRow(grid, row++, "Created",
            formatDate(metadata.getCreated()));

        addRow(grid, row++, "Last modified",
            formatDate(metadata.getModified()));

        addRow(grid, row++, "Last accessed",
            formatDate(metadata.getAccessed()));

        addRow(grid, row++, "Hidden",
            yesNo(metadata.isHidden()));

        addRow(grid, row++, "Readable",
            yesNo(metadata.isReadable()));

        addRow(grid, row++, "Writable",
            yesNo(metadata.isWritable()));

        addRow(grid, row++, "Executable",
            yesNo(metadata.isExecutable()));

        Button closeButton = new Button("Close");
        closeButton.setDefaultButton(true);
        closeButton.setOnAction(event -> stage.close());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(
            16, heading, new Separator(), grid,
            new Separator(), buttonBox
        );

        root.setPadding(new Insets(24));

        Scene scene = new Scene(root, 600, 560);

        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.showAndWait();
    }

    private void addRow(
            GridPane grid, int row, String key, String value) {

        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-font-weight: bold;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(380);

        grid.add(keyLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private String formatSize(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        double size = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unit = 0;

        while (size >= 1024 && unit < units.length - 1) {
            size /= 1024;
            unit++;
        }

        return String.format(
            java.util.Locale.ROOT,
            "%.2f %s",
            size,
            units[unit]
        );
    }

    private String formatDate(Instant instant) {
        return FORMATTER.format(
            instant.atZone(ZoneId.systemDefault())
        );
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
