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

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * <h2>背景</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public final class MacBackground {

    private final StackPane bg;
    private final Pane windows;

    private Runnable invalidationListener;

    public MacBackground(String url) {

        windows = new Pane();

        Region bg1 = new Region();

        Region bg2 = new Region();
        bg2.setStyle("-fx-background-image: url(" + url + ");");

        bg = new StackPane(bg1, bg2, windows);
    }

    public Region asNode() {
        return bg;
    }

    public void setImage(String url) {

        Node back = bg.getChildren().get(0);
        back.setOpacity(1);
        back.setStyle("-fx-background-image: url(" + url + ");");

        Node front = bg.getChildren().get(1);

        FadeTransition transition = new FadeTransition(Duration.seconds(1), front);
        transition.setFromValue(1);
        transition.setToValue(0);
        transition.currentTimeProperty().addListener(it -> repaint());
        transition.setOnFinished(e -> front.toBack());
        transition.play();
    }

    public void setOnInvalidationListener(Runnable runnable) {
        this.invalidationListener = Objects.requireNonNull(runnable);
    }

    public void repaint() {
        invalidationListener.run();
    }

    public Pane getWindows() {
        return windows;
    }
}