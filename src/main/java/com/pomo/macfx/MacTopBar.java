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

import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <h2>顶部栏</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public final class MacTopBar {

    private final BorderPane root;

    public MacTopBar() {

        root = new BorderPane();
        root.getStyleClass().add("top-bar");

        // region {left}
        Button apple = new Button();
        apple.getStyleClass().addAll("apple", "svg");
        apple.setOnAction(e -> ((Stage) root.getScene().getWindow()).setFullScreen(true));

        StackPane left = new StackPane(apple);
        left.getStyleClass().add("left");
        root.setLeft(left);
        // endregion

        // region {right}

        // region {time}
        Label time = new Label();
        time.getStyleClass().add("time");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                String format = localDateTime.format(formatter);

                time.setText(format);
            }
        };
        timer.start();
        // endregion

        // region {battery}
        Label battery = new Label("89%");
        battery.getStyleClass().addAll("battery");
        Region batteryGraphic = new Region();
        batteryGraphic.getStyleClass().addAll("graphic", "svg");
        battery.setGraphic(batteryGraphic);
        // endregion

        Button wifi = new Button();
        wifi.getStyleClass().addAll("wifi", "svg");

        Button bluetooth = new Button();
        bluetooth.getStyleClass().addAll("bluetooth", "svg");

        HBox right = new HBox(bluetooth, wifi, battery, time);
        right.getStyleClass().add("right");
        root.setRight(right);
        // endregion

        Label title = new Label("MacFX UI v1.0, by Mr.Po");
        StackPane center = new StackPane(title);
        root.setCenter(center);
    }

    public Region asNode() {
        return root;
    }
}
