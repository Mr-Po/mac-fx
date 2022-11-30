/*
 * mac-fx - use javafx to simulate macOS
 * Copyright © 2022 Mr.Po (ldd_live@foxmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pomo.macfx;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <h2>Dock栏</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public final class MacDock {

    public static final double MIN_TO_BOTTOM = -120;
    public static final double MAX_TO_BOTTOM = -80;

    public static final int MIN_BLUR = 0;
    public static final int MAX_BLUR = 3;

    // 这个值是测出来的，其主要是为了保证总是有偶数个点被update
    private final static double EYE_DISTANCE = 271;

    private final StackPane root;
    private final HBox container;

    private final DoubleProperty toBottom;
    private final IntegerProperty blur;

    private List<MacDockItem> items;

    public MacDock(MacBackground macBackground) {

        toBottom = new SimpleDoubleProperty(this, "toBottom", MIN_TO_BOTTOM);
        blur = new SimpleIntegerProperty(this, "blur", 1);

        // region {root}
        root = new StackPane();
        root.getStyleClass().add("dock");
        // root.setPickOnBounds(true);
        root.setPadding(new Insets(EYE_DISTANCE / 2));
        root.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        // endregion

        // region {DockItem容器}
        // 不应使用间隙，因为处于中间位置时-点击，无法判定
        container = new HBox();
        container.getStyleClass().add("container");
        container.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        // endregion

        // region {玻璃板}
        Rectangle grass = new Rectangle();
        grass.getStyleClass().add("grass");
        grass.setX(root.getPadding().getLeft());
        grass.widthProperty().bind(container.widthProperty());

        BoxBlur boxBlur = new BoxBlur();
        boxBlur.iterationsProperty().bind(blur);
        DropShadow shadow = new DropShadow();
        shadow.setInput(boxBlur);

        grass.setEffect(shadow);
        grass.setManaged(false);

        Rectangle grassBorder = new Rectangle();
        grassBorder.getStyleClass().add("grass-border");
        grassBorder.xProperty().bind(grass.xProperty());
        grassBorder.yProperty().bind(grass.yProperty());
        grassBorder.heightProperty().bind(grass.heightProperty());
        grassBorder.widthProperty().bind(grass.widthProperty());
        grassBorder.setManaged(false);
        // endregion


        ObservableList<Node> children = root.getChildren();
        children.addAll(grass, grassBorder, container);

        DockTip dockTip = new DockTip();
        Node tip = dockTip.asNode();
        tip.setManaged(false);

        root.setOnMouseExited(e -> items.forEach(MacDockItem::reset));
        root.setOnMouseMoved(e -> {

            double x = e.getX();

            double distanceV = container.getLayoutY() + container.getHeight() / 5;
            double percentY = 1 - Math.abs(e.getY() - distanceV) / distanceV;

            boolean selected = false;

            for (MacDockItem item : items) {

                Node node = item.asNode();
                Bounds bounds = root.sceneToLocal(node.localToScene(node.getBoundsInLocal()));

                // 当前选中
                if (bounds.contains(x, e.getY())) {

                    selected = true;

                    item.update(1 * percentY);

                    if (!children.contains(tip)) {
                        children.add(tip);
                    }
                    dockTip.setText(item.getName());

                    Bounds tipBounds = tip.getBoundsInLocal();
                    tip.relocate(
                            bounds.getMinX() + bounds.getWidth() / 2 - tipBounds.getWidth() / 2,
                            bounds.getMinY() - tipBounds.getHeight()
                    );

                } else {

                    // 鼠标到近边的距离
                    double distanceX = Math.min(Math.abs(x - bounds.getMinX()), Math.abs(x - bounds.getMaxX()));
                    double percentX = 1 - distanceX / EYE_DISTANCE;

                    item.update(percentX * percentY);
                }

                if (!selected) {
                    children.remove(tip);
                }
            }
        });

        Region bg = macBackground.asNode();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(null);

        ObjectBinding<Paint> fillBinding = Bindings.createObjectBinding(() -> {

            Bounds bounds = grass.localToScene(grass.getLayoutBounds());

            Rectangle2D viewport = new Rectangle2D(
                    bounds.getMinX(), bounds.getMinY(),
                    bounds.getWidth(), bounds.getHeight()
            );
            parameters.setViewport(viewport);


            try {

                return new ImagePattern(bg.snapshot(parameters, null));

            } catch (NullPointerException ignored) {

                return null;
            }

        }, grass.widthProperty());

        macBackground.setOnInvalidationListener(fillBinding::invalidate);

        // 下一次才能得到正确的高度
        Platform.runLater(() -> {

            grass.setHeight(container.getHeight());

            grass.yProperty().bind(root.heightProperty()
                    .subtract(grass.heightProperty())
                    .subtract(root.getPadding().getBottom())
                    .add(5)
            );

            grass.fillProperty().bind(fillBinding);
        });
    }

    public void setItems(List<MacDockItem> items) {

        this.items = Objects.requireNonNull(items);

        container.getChildren().addAll(items.stream().map(MacDockItem::asNode).collect(Collectors.toList()));
    }

    public Node asNode() {
        return root;
    }

    public double getToBottom() {
        return toBottom.get();
    }

    public DoubleProperty toBottomProperty() {
        return toBottom;
    }

    public void setToBottom(double toBottom) {
        this.toBottom.set(toBottom);
    }

    public int getBlur() {
        return blur.get();
    }

    public IntegerProperty blurProperty() {
        return blur;
    }

    public void setBlur(int blur) {
        this.blur.set(blur);
    }

    private final static class DockTip {

        private final Group root;
        private final Label label;

        public DockTip() {

            label = new Label();
            root = new Group(new Group(), label);
            root.getStyleClass().add("tip");

            ObjectBinding<Node> graphicBinding = Bindings.createObjectBinding(() -> {

                Rectangle rectangle = new Rectangle(label.getWidth() + 20, label.getHeight() + 10);
                rectangle.setArcWidth(10);
                rectangle.setArcHeight(10);

                Polygon polygon = new Polygon(0, 0, 20, 0, 10, 10);
                polygon.setTranslateX(rectangle.getWidth() / 2 - 10);
                polygon.setTranslateY(rectangle.getHeight());

                Shape shape = Shape.union(rectangle, polygon);
                shape.getStyleClass().add("border");
                shape.setTranslateX(-10);
                shape.setTranslateY(-3);

                return shape;

            }, label.widthProperty(), label.heightProperty());

            graphicBinding.addListener((observable, oldValue, newValue) ->
                    root.getChildren().set(0, newValue));

            label.setUserData(graphicBinding);
        }

        public Node asNode() {
            return root;
        }

        public void setText(String text) {
            label.setText(text);
        }
    }
}