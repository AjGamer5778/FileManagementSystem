package app;

import filesystem.FileSystem;
import models.FileSystemItem;
import models.FolderItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

final class FolderGraphView extends Pane {
    private static final double CARD_WIDTH = 210;
    private static final double CARD_HEIGHT = 48;
    private static final double MARGIN = 36;
    private static final double MAX_CARD_WIDTH = 390;
    private static final double MIN_RING_GAP = MAX_CARD_WIDTH + 40;
    private static final double NODE_ANGULAR_SPACING = MAX_CARD_WIDTH + 40;

    private final FileSystem fileSystem;
    private final Consumer<String> statusReporter;
    private final Consumer<IOException> errorReporter;
    private boolean darkMode;
    private GraphNode rootNode;

    FolderGraphView(FileSystem fileSystem, Consumer<IOException> errorReporter,
            Consumer<String> statusReporter) {
        this.fileSystem = fileSystem;
        this.errorReporter = errorReporter;
        this.statusReporter = statusReporter;
    }

    void setWorkspace(FolderItem workspace) throws IOException {
        int childCount = fileSystem.listContents(workspace.getPath()).size();
        rootNode = new GraphNode(workspace, childCount);
        statusReporter.accept("Click a folder node to expand or collapse it.");
        renderGraph();
    }

    void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        renderGraph();
    }

    private void toggleFolder(GraphNode node) {
        if (node.expanded) {
            node.children.clear();
            node.expanded = false;
        } else {
            try {
                ArrayList<FileSystemItem> items = fileSystem.listContents(node.item.getPath());
                node.childCount = items.size();
                for (FileSystemItem item : items) {
                    int childCount = item instanceof FolderItem ? -1 : 0;
                    node.children.add(new GraphNode(item, childCount));
                }
                node.expanded = true;
            } catch (IOException exception) {
                errorReporter.accept(exception);
                return;
            }
        }

        if (node.expanded && node.children.isEmpty()) {
            String folderName = node.item.getPath().getFileName() == null
                    ? node.item.getPath().toString()
                    : node.item.getName();
            statusReporter.accept(folderName + " is empty.");
        } else {
            statusReporter.accept("Click a folder node to expand or collapse it  ·  Scroll to explore");
        }
        renderGraph();
    }

    private void renderGraph() {
        getChildren().clear();
        setStyle("-fx-background-color: " + (darkMode ? "#171a21" : "#f4f6f8") + ";");
        if (rootNode == null) {
            showEmptyState();
            return;
        }

        Map<Integer, Integer> nodesPerDepth = new HashMap<>();
        calculateVisibleTree(rootNode, 0, nodesPerDepth);
        int maxDepth = nodesPerDepth.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        double[] ringRadii = calculateRingRadii(nodesPerDepth, maxDepth);
        double outerRadius = ringRadii[maxDepth];
        double center = outerRadius + MAX_CARD_WIDTH / 2 + MARGIN;
        placeRadial(rootNode, 0, center, center, -Math.PI, Math.PI, ringRadii);
        setGraphSize(center * 2, center * 2);

        List<GraphNode> nodes = visibleNodes(rootNode);
        for (GraphNode node : nodes) {
            drawConnections(node);
        }
        for (GraphNode node : nodes) {
            drawNode(node);
        }
    }

    private void showEmptyState() {
        Label emptyState = new Label("Choose a folder to start exploring");
        emptyState.setStyle("-fx-font-size: 16px; -fx-text-fill: "
                + (darkMode ? "#9ba9bb" : "#64748b") + ";");
        emptyState.setLayoutX(MARGIN);
        emptyState.setLayoutY(MARGIN);
        setGraphSize(800, 480);
        getChildren().add(emptyState);
    }

    private void setGraphSize(double width, double height) {
        setMinSize(width, height);
        setPrefSize(width, height);
    }

    private double[] calculateRingRadii(Map<Integer, Integer> nodesPerDepth, int maxDepth) {
        double[] ringRadii = new double[maxDepth + 1];
        for (int depth = 1; depth <= maxDepth; depth++) {
            int nodeCount = nodesPerDepth.getOrDefault(depth, 1);
            double spacingRadius = nodeCount * NODE_ANGULAR_SPACING / (2 * Math.PI);
            ringRadii[depth] = ringRadii[depth - 1] + Math.max(MIN_RING_GAP, spacingRadius);
        }
        return ringRadii;
    }

    private void calculateVisibleTree(GraphNode node, int depth, Map<Integer, Integer> nodesPerDepth) {
        nodesPerDepth.merge(depth, 1, Integer::sum);
        if (!node.expanded || node.children.isEmpty()) {
            return;
        }

        for (GraphNode child : node.children) {
            calculateVisibleTree(child, depth + 1, nodesPerDepth);
        }
    }

    private int countVisibleLeaves(GraphNode node) {
        if (!node.expanded || node.children.isEmpty()) {
            return 1;
        }

        int leaves = 0;
        for (GraphNode child : node.children) {
            leaves += countVisibleLeaves(child);
        }
        return leaves;
    }

    private void placeRadial(GraphNode node, int depth, double centerX, double centerY,
            double angleStart, double angleEnd, double[] ringRadii) {
        node.centerX = depth == 0 ? centerX : centerX + ringRadii[depth] * Math.cos(node.angle);
        node.centerY = depth == 0 ? centerY : centerY + ringRadii[depth] * Math.sin(node.angle);
        if (!node.expanded || node.children.isEmpty()) {
            return;
        }

        int totalLeaves = countVisibleLeaves(node);
        double angle = angleStart;
        for (GraphNode child : node.children) {
            double childAngleEnd = angle + (angleEnd - angleStart)
                    * countVisibleLeaves(child) / totalLeaves;
            child.angle = (angle + childAngleEnd) / 2;
            placeRadial(child, depth + 1, centerX, centerY,
                    angle, childAngleEnd, ringRadii);
            angle = childAngleEnd;
        }
    }

    private List<GraphNode> visibleNodes(GraphNode node) {
        List<GraphNode> nodes = new ArrayList<>();
        nodes.add(node);
        if (node.expanded) {
            for (GraphNode child : node.children) {
                nodes.addAll(visibleNodes(child));
            }
        }
        return nodes;
    }

    private void drawConnections(GraphNode parent) {
        if (!parent.expanded || parent.children.isEmpty()) {
            return;
        }
        for (GraphNode child : parent.children) {
            Line connection = new Line(
                    parent.centerX, parent.centerY, child.centerX, child.centerY);
            connection.setStroke(themeColor("#718096", "#91a0b7"));
            connection.setStrokeWidth(1.4 + Math.min(5.6, parent.childCount * 0.18));
            getChildren().add(connection);
        }
    }

    private void drawNode(GraphNode node) {
        boolean folder = node.item instanceof FolderItem;
        double cardWidth = nodeWidth(node);
        double cardHeight = nodeHeight(node);
        String name = node.item.getPath().getFileName() == null
                ? node.item.getPath().toString()
                : node.item.getName();
        String labelText = folder
                ? (node.expanded ? "[-] " : "[+] ") + name
                        + (node.childCount >= 0 ? "  ·  " + node.childCount : "")
                : name;

        Rectangle background = new Rectangle(cardWidth, cardHeight);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setFill(folder
                ? themeColor("#e8f1ff", "#263a57")
                : themeColor("#ffffff", "#2a303b"));
        background.setStroke(themeColor("#59708b", "#8193aa"));
        background.setStrokeWidth(1.1);

        Label label = new Label(labelText);
        label.setMaxWidth(cardWidth - 20);
        label.setPadding(new Insets(0, 10, 0, 10));
        label.setStyle("-fx-text-fill: " + (darkMode ? "#e6ebf2" : "#17212f")
                + "; -fx-font-size: 13px; -fx-font-weight: "
                + (folder ? "bold" : "normal") + ";");
        label.setTooltip(new Tooltip(node.item.getPath().toString()));

        StackPane card = new StackPane(background, label);
        card.setAlignment(Pos.CENTER_LEFT);
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        card.setMinSize(cardWidth, cardHeight);
        card.setPrefSize(cardWidth, cardHeight);
        card.setMaxSize(cardWidth, cardHeight);
        card.setLayoutX(node.centerX - cardWidth / 2);
        card.setLayoutY(node.centerY - cardHeight / 2);
        card.setEffect(new DropShadow(10, 0, 3,
                themeColor("#18315322", "#00000055")));

        if (folder) {
            configureFolderCard(card, node);
        }
        getChildren().add(card);
        showEmptyFolderHint(node, folder, cardWidth, cardHeight);
    }

    private void configureFolderCard(StackPane card, GraphNode node) {
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                toggleFolder(node);
            }
        });
        card.setOnMouseEntered(event -> {
            card.setScaleX(1.025);
            card.setScaleY(1.025);
        });
        card.setOnMouseExited(event -> {
            card.setScaleX(1);
            card.setScaleY(1);
        });
    }

    private void showEmptyFolderHint(GraphNode node, boolean folder,
            double cardWidth, double cardHeight) {
        if (!folder || !node.expanded || !node.children.isEmpty()) {
            return;
        }

        Label emptyHint = new Label("This folder is empty");
        emptyHint.setStyle("-fx-font-size: 11px; -fx-text-fill: "
                + (darkMode ? "#9ba9bb" : "#64748b") + ";");
        emptyHint.setLayoutX(node.centerX - cardWidth / 2 + 10);
        emptyHint.setLayoutY(node.centerY + cardHeight / 2 + 7);
        getChildren().add(emptyHint);
    }

    private double nodeWidth(GraphNode node) {
        return CARD_WIDTH + Math.min(180, Math.max(0, node.childCount) * 4.0);
    }

    private double nodeHeight(GraphNode node) {
        return CARD_HEIGHT + Math.min(36, Math.max(0, node.childCount) * 0.8);
    }

    private Color themeColor(String lightColor, String darkColor) {
        return Color.web(darkMode ? darkColor : lightColor);
    }

    private static final class GraphNode {
        private final FileSystemItem item;
        private final List<GraphNode> children = new ArrayList<>();
        private int childCount;
        private boolean expanded;
        private double centerX;
        private double centerY;
        private double angle;

        private GraphNode(FileSystemItem item, int childCount) {
            this.item = item;
            this.childCount = childCount;
        }
    }
}
