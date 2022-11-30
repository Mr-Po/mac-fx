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

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * <h2>图片窗口</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public final class MacImageWindow extends MacWindow {

    public MacImageWindow(MacBackground macBackground, Node source, Image image, String name, String url) {
        super(macBackground, name, source, createContent(macBackground, image, url));
    }

    private static Node createContent(MacBackground macBackground, Image image, String url) {

        ImageView imageView = new ImageView(image);

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Separator separator = new Separator();

        Button button = new Button("应用为背景");
        button.setOnAction(e -> macBackground.setImage(url));

        VBox content = new VBox(imageContainer, separator, button);
        content.getStyleClass().add("image-content");

        return content;
    }
}