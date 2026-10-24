package app;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

final class GraphZoomController {
    private final ScrollPane viewport;
    private final Pane graph;
    private final Label zoomLabel;
    private final DoubleProperty zoomLevel = new SimpleDoubleProperty(1.0);
    private final Scale scale = new Scale(1, 1, 0, 0);
    private Timeline zoomAnimation;

    GraphZoomController(ScrollPane viewport, Pane graph, Label zoomLabel) {
        this.viewport = viewport;
        this.graph = graph;
        this.zoomLabel = zoomLabel;

        scale.xProperty().bind(zoomLevel);
        scale.yProperty().bind(zoomLevel);
        graph.getTransforms().add(scale);
        zoomLevel.addListener((observable, oldValue, newValue) ->
                zoomLabel.setText(Math.round(newValue.doubleValue() * 100) + "%"));
    }

    void bindControls(Button zoomOut, Button zoomIn, Button reset) {
        zoomOut.setOnAction(event -> zoomAtViewportCenter(zoomLevel.get() - 0.1, true));
        zoomIn.setOnAction(event -> zoomAtViewportCenter(zoomLevel.get() + 0.1, true));
        reset.setOnAction(event -> zoomAtViewportCenter(1.0, true));
    }

    void attachGestures() {
        viewport.addEventFilter(ZoomEvent.ZOOM, event -> {
            zoomAt(event.getZoomFactor() * zoomLevel.get(),
                    event.getSceneX(), event.getSceneY(), false);
            event.consume();
        });
        viewport.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double factor = Math.exp(event.getDeltaY() * 0.002);
                zoomAt(factor * zoomLevel.get(), event.getSceneX(), event.getSceneY(), false);
                event.consume();
            }
        });
    }

    private void zoomAtViewportCenter(double targetZoom, boolean animate) {
        Bounds bounds = viewport.getViewportBounds();
        Point2D scenePoint = viewport.localToScene(
                bounds.getMinX() + bounds.getWidth() / 2,
                bounds.getMinY() + bounds.getHeight() / 2);
        zoomAt(targetZoom, scenePoint.getX(), scenePoint.getY(), animate);
    }

    private void zoomAt(double requestedZoom, double sceneX, double sceneY, boolean animate) {
        double targetZoom = Math.max(0.4, Math.min(2.5, requestedZoom));
        if (Math.abs(targetZoom - zoomLevel.get()) < 0.001) {
            return;
        }

        if (zoomAnimation != null) {
            zoomAnimation.stop();
        }

        Point2D pivot = graph.sceneToLocal(sceneX, sceneY);
        scale.setPivotX(pivot.getX());
        scale.setPivotY(pivot.getY());

        if (animate) {
            zoomAnimation = new Timeline(new KeyFrame(Duration.millis(170),
                    new KeyValue(zoomLevel, targetZoom, Interpolator.EASE_BOTH)));
            zoomAnimation.play();
        } else {
            zoomLevel.set(targetZoom);
        }
    }
}
