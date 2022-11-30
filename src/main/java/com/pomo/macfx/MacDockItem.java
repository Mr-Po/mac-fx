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

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Objects;
import java.util.function.Function;

/**
 * <h2>Dock栏子项</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public final class MacDockItem {

    private final static AudioClip AUDIO_OPEN = new AudioClip(MacDockItem.class.getResource("/com/pomo/macfx/sound/open.wav").toExternalForm());

    private final static AudioClip AUDIO_RECOVER = new AudioClip(MacDockItem.class.getResource("/com/pomo/macfx/sound/recover.wav").toExternalForm());

    private final static AudioClip AUDIO_MENU = new AudioClip(MacDockItem.class.getResource("/com/pomo/macfx/sound/menu.wav").toExternalForm());

    private final static double BASE_SIZE = 100;

    private final static double BASE_SCALE = 0.4;
    private final static double EXT_SCALE = 0.3;

    private final static double BASE_FRACTION = 0;
    private final static double EXT_FRACTION = 0.25;

    private final static double BASE_TOP_OFFSET = 9;
    private final static double EXT_TOP_OFFSET = -5;

    private final Region root;
    private final Group group;

    private final DoubleProperty fractionProperty;
    private final DoubleProperty topOffsetProperty;

    private final String name;

    private final Circle dot;

    private MacWindow macWindow;

    public MacDockItem(MacBackground macBackground, Image image, String name,
                       Function<Node, MacWindow> supplier) {

        this.name = Objects.requireNonNull(name);

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(BASE_SIZE * 2);
        imageView.setFitHeight(BASE_SIZE * 2);
        imageView.setClip(new Circle(BASE_SIZE, BASE_SIZE, BASE_SIZE));

        Shape circleBorder = createCircleBorder();

        group = new Group(imageView, circleBorder);

        group.setScaleX(BASE_SCALE);
        group.setScaleY(BASE_SCALE);


        Reflection reflection = new Reflection(BASE_TOP_OFFSET, BASE_FRACTION, 0.4, 0);
        fractionProperty = reflection.fractionProperty();
        topOffsetProperty = reflection.topOffsetProperty();

        group.setEffect(reflection);

        dot = new Circle(4);
        dot.getStyleClass().add("dot");
        dot.setVisible(false);

        root = new StackPane(new Group(group), dot);
        root.getStyleClass().add("dock-item");
        root.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        root.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                AUDIO_MENU.play();
            }
        });

        root.setOnMouseClicked(e -> {

            if (macWindow != null) {

                if (macWindow.getState() == MacWindow.State.HIDE) {
                    macWindow.recover();
                    AUDIO_RECOVER.play();
                }

            } else {

                macWindow = supplier.apply(root);
                showFromFirst(macBackground.getWindows(), macWindow, root);
                dot.setVisible(true);
            }
        });
    }

    public Node asNode() {
        return root;
    }

    public void update(double percent) {

        if (percent < 0) {

            reset();
            return;

        } else if (percent > 1) {

            percent = 1;
        }

        double scale = EXT_SCALE * percent + BASE_SCALE;
        group.setScaleX(scale);
        group.setScaleY(scale);

        fractionProperty.set(EXT_FRACTION * percent + BASE_FRACTION);
        topOffsetProperty.set(EXT_TOP_OFFSET * percent + BASE_TOP_OFFSET);
    }

    public void reset() {

        group.setScaleX(BASE_SCALE);
        group.setScaleY(BASE_SCALE);

        fractionProperty.set(BASE_FRACTION);
    }

    private Shape createCircleBorder() {

        Circle circle1 = new Circle(0, 0, BASE_SIZE);
        Circle circle2 = new Circle(0, 0, BASE_SIZE - 10);

        Shape shape = Shape.subtract(circle1, circle2);
        shape.setFill(Color.WHITE);
        shape.setStroke(Color.LIGHTGRAY);
        shape.setStrokeWidth(2.5);
        shape.setLayoutX(BASE_SIZE);
        shape.setLayoutY(BASE_SIZE);

        return shape;
    }

    private void showFromFirst(Pane windows, MacWindow macWindow, Node source) {

        Region window = macWindow.asNode();
        windows.getChildren().add(window);
        macWindow.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == MacWindow.State.DESTROY) {
                windows.getChildren().remove(window);
                this.macWindow = null;
                dot.setVisible(false);
            }
        });

        windows.applyCss();
        windows.layout();

        double halfWidth = window.getWidth() / 2;
        double halfHeight = window.getHeight() / 2;

        Circle clip = new Circle(halfWidth, halfHeight, 15);
        window.setClip(clip);

        Bounds bounds = windows.sceneToLocal(source.localToScene(source.getBoundsInLocal()));

        window.setLayoutX(bounds.getMinX() + bounds.getWidth() / 2 - halfWidth);
        window.setLayoutY(bounds.getMinY() + bounds.getHeight() / 2 - halfHeight);

        Rotate rotateX = new Rotate(180, halfWidth, halfHeight, 0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(180, halfWidth, halfHeight, 0, Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(180, halfWidth, halfHeight, 0, Rotate.Z_AXIS);
        window.getTransforms().addAll(
                rotateX,
                rotateY,
                rotateZ
        );

        Timeline timeline1 = new Timeline(new KeyFrame(Duration.seconds(0.5),
                new KeyValue(rotateX.angleProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(rotateY.angleProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(rotateZ.angleProperty(), 0, Interpolator.EASE_IN),
                new KeyValue(clip.radiusProperty(), 35, Interpolator.EASE_IN)
        ));

        Timeline timeline2 = new Timeline(new KeyFrame(Duration.seconds(0.7),
                new KeyValue(window.layoutXProperty(),
                        windows.getWidth() / 2 - halfWidth, Interpolator.LINEAR),
                new KeyValue(window.layoutYProperty(),
                        windows.getHeight() / 2 - halfHeight - 100, Interpolator.LINEAR)
        ));

        Timeline timeline3 = new Timeline(new KeyFrame(Duration.seconds(0.3),
                new KeyValue(clip.radiusProperty(),
                        Math.max(halfWidth, halfHeight) * 5, Interpolator.EASE_OUT)
        ));

        timeline1.setOnFinished(e -> AUDIO_OPEN.play());

        timeline2.setOnFinished(e -> timeline3.play());
        timeline3.setOnFinished(e -> window.setClip(null));

        timeline1.play();
        timeline2.play();
    }

    public String getName() {
        return name;
    }
}