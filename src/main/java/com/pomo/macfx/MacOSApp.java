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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <h2>主程序</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public class MacOSApp extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        StackPane root = new StackPane();

        primaryStage.getIcons().add(new Image("/com/pomo/macfx/icon.jpg"));

        // region {背景}
        MacBackground macBackground = new MacBackground("/com/pomo/macfx/img/original/1.jpg");
        Region bg = macBackground.asNode();
        // endregion

        // region {顶部栏}
        MacTopBar macTopBar = new MacTopBar();
        Region topBar = macTopBar.asNode();
        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        topBar.setMaxHeight(Region.USE_PREF_SIZE);
        // topBar.prefWidthProperty().bind(root.widthProperty());
        // endregion


        MacDock macDock = new MacDock(macBackground);
        Node dock = macDock.asNode();
        StackPane.setAlignment(dock, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dock, new Insets(0, 0, macDock.getToBottom(), 0));
        macDock.toBottomProperty().addListener((observable, oldValue, newValue) ->
                StackPane.setMargin(dock, new Insets(0, 0, newValue.doubleValue(), 0)));

        root.getChildren().addAll(bg, topBar, dock);

        Scene scene = new Scene(root, 1366, 866);
        scene.getStylesheets().add("com/pomo/macfx/dock.css");

        Image cursor = new Image("/com/pomo/macfx/cursor.png");
        scene.setCursor(new ImageCursor(cursor));

        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX 仿 macOS 桌面");
        primaryStage.show();

        List<MacDockItem> items = IntStream.range(1, 9)
                .mapToObj(it -> createImageDockItem(macBackground, it))
                .collect(Collectors.toList());
        items.add(createSettingDockItem(macBackground, macDock));

        macDock.setItems(items);
    }

    private static MacDockItem createSettingDockItem(MacBackground macBackground, MacDock macDock) {

        Image image = new Image("/com/pomo/macfx/img/setting.jpg");
        String name = "设置";


        return new MacDockItem(macBackground, image, name,
                source -> new MacSettingWindow(macBackground, name, source, macDock)
        );
    }

    private static MacDockItem createImageDockItem(MacBackground macBackground, int no) {

        Image image = new Image("/com/pomo/macfx/img/" + no + ".jpg");
        String name = "图片：" + no + ".jpg";

        return new MacDockItem(macBackground, image, name,
                source -> new MacImageWindow(macBackground, source, image, name, getOriginalPath(no))
        );
    }

    private static String getOriginalPath(int no) {
        return "/com/pomo/macfx/img/original/" + no + ".jpg";
    }
}
