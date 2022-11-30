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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * <h2>mac窗口</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public class MacWindow {

    private final static AudioClip AUDIO_CLOSE = new AudioClip(MacDockItem.class.getResource("/com/pomo/macfx/sound/close.wav").toExternalForm());
    private final static AudioClip AUDIO_MINIMIZE = new AudioClip(MacDockItem.class.getResource("/com/pomo/macfx/sound/minimize.wav").toExternalForm());

    private final VBox root;
    private final ReadOnlyObjectWrapper<State> state;

    private double offsetX;
    private double offsetY;

    private Minimizer minimizer;

    public MacWindow(MacBackground macBackground, String name, Node source, Node content) {

        state = new ReadOnlyObjectWrapper<>(this, "state", State.SHOW);

        SimpleBooleanProperty hoverProperty = new SimpleBooleanProperty();

        Button close = createButton(hoverProperty, "close");
        Button minimize = createButton(hoverProperty, "minimize");
        Button maximize = createButton(hoverProperty, "maximize");

        HBox toolPane = new HBox(close, minimize, maximize);
        toolPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        hoverProperty.bind(toolPane.hoverProperty());
        toolPane.getStyleClass().add("tool-pane");
        StackPane.setAlignment(toolPane, Pos.CENTER_LEFT);

        Label label = new Label(name);
        label.getStyleClass().add("title");

        StackPane titleBar = new StackPane(toolPane, label);
        titleBar.getStyleClass().add("title-bar");

        StackPane container = new StackPane(content);
        container.getStyleClass().add("container");

        root = new VBox(titleBar, container);
        root.getStyleClass().add("window");
        root.setAlignment(Pos.CENTER);
        root.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> root.toFront());

        DropShadow dropShadow = new DropShadow(
                BlurType.THREE_PASS_BOX,
                Color.rgb(0, 0, 0, 0.5),
                50, 0, 0, 0
        );
        root.setEffect(dropShadow);

        titleBar.setOnMousePressed(e -> {
            offsetX = e.getX();
            offsetY = e.getY();
        });

        titleBar.setOnMouseDragged(e -> {
            root.relocate(
                    root.getLayoutX() + e.getX() - offsetX,
                    root.getLayoutY() + e.getY() - offsetY
            );
            macBackground.repaint();
        });

        // region {关闭}
        close.setOnAction(e -> {

            if (state.get() != State.SHOW) {
                return;
            }

            AUDIO_CLOSE.play();

            Bounds windowBounds = root.getBoundsInLocal();

            Bounds buttonBounds = root.sceneToLocal(close.localToScene(close.getBoundsInLocal()));
            double x = buttonBounds.getMinX() + buttonBounds.getWidth() / 2;
            double y = buttonBounds.getMinY() + buttonBounds.getHeight() / 2;

            Circle clip = new Circle(x, y, Math.max(windowBounds.getWidth(), windowBounds.getHeight()));
            clip.setFill(new RadialGradient(
                    0, 0, 0.5, 0.5, 1, true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.BLACK),
                    new Stop(0.9, Color.TRANSPARENT))
            );
            root.setClip(clip);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.3),
                    new KeyValue(clip.radiusProperty(), 1, Interpolator.EASE_OUT)
            ));
            timeline.currentTimeProperty().addListener(it -> macBackground.repaint());
            timeline.setOnFinished(it -> state.set(State.DESTROY));
            timeline.play();
        });
        // endregion

        // region {最小化}
        minimize.setOnAction(e -> {

            if (minimizer == null) {

                minimizer = new Minimizer() {

                    private final Timeline timeline1;
                    private final Timeline timeline2;
                    private final PerspectiveTransform transform;
                    private final double width;
                    private final double height;
                    private final Pane windows;

                    {
                        windows = macBackground.getWindows();

                        timeline1 = new Timeline();
                        timeline1.currentTimeProperty().addListener(it -> macBackground.repaint());

                        timeline2 = new Timeline();
                        timeline2.currentTimeProperty().addListener(it -> macBackground.repaint());

                        transform = new PerspectiveTransform();
                        transform.setInput(dropShadow);

                        width = root.getWidth();
                        height = root.getHeight();

                        timeline1.setOnFinished(it -> {

                            timeline1.setRate(-timeline1.getRate());

                            if (timeline1.getRate() == -1) {

                                timeline2.play();

                            } else {

                                root.setEffect(dropShadow);
                                postHandle();

                                state.set(State.SHOW);
                            }
                        });

                        timeline2.setOnFinished(it -> {

                            timeline2.setRate(-timeline2.getRate());

                            if (timeline2.getRate() == 1) {

                                transform.setLlx(0);
                                transform.setLly(height);
                                transform.setLrx(width);
                                transform.setLry(height);

                                timeline1.play();

                            } else {

                                windows.getChildren().remove(root);
                                postHandle();

                                state.set(State.HIDE);
                            }
                        });
                    }

                    private void postHandle() {

                        timeline1.getKeyFrames().clear();
                        timeline2.getKeyFrames().clear();

                        macBackground.repaint();
                    }

                    private void preHandle() {

                        Bounds bounds = windows.sceneToLocal(source.localToScene(source.getBoundsInLocal()));

                        // 因为是相对于root的locale，所以要-去自身坐标，才是从父的0开始
                        double x = bounds.getMinX() + bounds.getWidth() / 2 - root.getLayoutX();
                        double y = bounds.getMinY() + bounds.getHeight() / 2 - root.getLayoutY();

                        timeline1.getKeyFrames().add(new KeyFrame(Duration.seconds(0.15),
                                new KeyValue(transform.llxProperty(), x - 5, Interpolator.EASE_IN),
                                new KeyValue(transform.llyProperty(), y, Interpolator.EASE_IN),
                                new KeyValue(transform.lrxProperty(), x + 5, Interpolator.EASE_IN),
                                new KeyValue(transform.lryProperty(), y, Interpolator.EASE_IN)
                        ));

                        timeline2.getKeyFrames().add(new KeyFrame(Duration.seconds(0.25),
                                new KeyValue(transform.ulxProperty(), x - 10, Interpolator.EASE_OUT),
                                new KeyValue(transform.ulyProperty(), y, Interpolator.EASE_OUT),
                                new KeyValue(transform.urxProperty(), x + 10, Interpolator.EASE_OUT),
                                new KeyValue(transform.uryProperty(), y, Interpolator.EASE_OUT)
                        ));
                    }

                    @Override
                    public void minimize() {

                        AUDIO_MINIMIZE.play();

                        root.setEffect(transform);

                        transform.setUlx(0);
                        transform.setUly(0);
                        transform.setUrx(width);
                        transform.setUry(0);

                        transform.setLlx(0);
                        transform.setLly(height);
                        transform.setLrx(width);
                        transform.setLry(height);

                        preHandle();

                        timeline1.play();
                    }

                    @Override
                    public void recover() {

                        windows.getChildren().add(root);


                        transform.setUlx(0);
                        transform.setUly(0);
                        transform.setUrx(width);
                        transform.setUry(0);

                        preHandle();

                        timeline2.play();
                    }
                };
            }

            minimizer.minimize();
        });
        // endregion
    }

    public final Region asNode() {
        return root;
    }

    public final void recover() {
        minimizer.recover();
    }

    public State getState() {
        return state.get();
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    private static Button createButton(BooleanProperty hoverProperty, String className) {

        Circle circle = new Circle(10);

        Region region = new Region();
        region.visibleProperty().bind(hoverProperty);

        StackPane stackPane = new StackPane(circle, region);

        Button button = new Button();
        button.getStyleClass().add(className);
        button.setGraphic(stackPane);

        return button;
    }

    private interface Minimizer {

        void minimize();

        void recover();
    }

    public enum State {
        SHOW,
        HIDE,
        DESTROY
    }
}