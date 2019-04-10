package com.bright.administrator.lib_common.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.List;

public class StringAxisValueFormatter implements IAxisValueFormatter {
    private List<String> xValues;

    public StringAxisValueFormatter(List<String> xValues) {
        this.xValues = xValues;
    }

    @Override
    public String getFormattedValue(float v, AxisBase axisBase) {

        if (v < 0 || v > (xValues.size() - 1)){//使得两侧柱子完全显示
            return "";
        }
        int i = (int) v % xValues.size();
        return xValues.get(i);
    }
}
