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

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.StringConverter;

/**
 * <h2>设置窗口</h2>
 *
 * @author Mr.Po, ldd_live@foxmail.com
 */
public class MacSettingWindow extends MacWindow {

    public MacSettingWindow(MacBackground macBackground, String name,
                            Node source, MacDock macDock) {
        super(macBackground, name, source, createContent(macBackground, macDock));
    }

    private static Node createContent(MacBackground macBackground, MacDock macDock) {


        GridPane content = new GridPane();
        content.getStyleClass().add("setting-content");

        // region {toBottomSlider}
        Slider toBottomSlider = new Slider(MacDock.MIN_TO_BOTTOM, MacDock.MAX_TO_BOTTOM, macDock.getToBottom());
        toBottomSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {

                if (MacDock.MIN_TO_BOTTOM == object) {
                    return "低";
                } else {
                    return "高";
                }
            }

            @Override
            public Double fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
        toBottomSlider.setSnapToTicks(false);
        toBottomSlider.setMajorTickUnit(MacDock.MAX_TO_BOTTOM - MacDock.MIN_TO_BOTTOM);
        toBottomSlider.setMinorTickCount(0);
        toBottomSlider.setBlockIncrement(1);
        toBottomSlider.setShowTickMarks(true);
        toBottomSlider.setShowTickLabels(true);

        macDock.toBottomProperty().bind(toBottomSlider.valueProperty());
        toBottomSlider.valueProperty().addListener(it -> macBackground.repaint());
        content.addRow(0, new Label("距离底部距离："), toBottomSlider);
        // endregion

        // region {blurSlider}
        Slider blurSlider = new Slider(MacDock.MIN_BLUR, MacDock.MAX_BLUR, macDock.getBlur());
        blurSlider.setSnapToTicks(true);
        blurSlider.setMajorTickUnit(1);
        blurSlider.setMinorTickCount(0);
        blurSlider.setBlockIncrement(1);
        blurSlider.setShowTickMarks(true);
        blurSlider.setShowTickLabels(true);

        macDock.blurProperty().bind(blurSlider.valueProperty());
        blurSlider.valueProperty().addListener(it -> macBackground.repaint());
        content.addRow(1, new Label("模糊等级："), blurSlider);
        // endregion

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHalignment(HPos.RIGHT);
        content.getColumnConstraints().add(column1);

        RowConstraints row1 = new RowConstraints();
        row1.setValignment(VPos.TOP);

        RowConstraints row2 = new RowConstraints();
        row2.setValignment(VPos.TOP);

        content.getRowConstraints().addAll(row1, row2);

        return content;
    }
}
