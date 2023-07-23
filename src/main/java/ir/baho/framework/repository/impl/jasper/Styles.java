package ir.baho.framework.repository.impl.jasper;

import ir.baho.framework.metadata.report.Border;
import ir.baho.framework.metadata.report.Style;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;

import java.time.temporal.Temporal;

public class Styles {

    public static Style title() {
        Style style = new Style();
        style.getTextFormat().setFontSize(20);
        style.getTextFormat().setBold(true);
        return style;
    }

    public static Style subtitle(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        return style;
    }

    public static Style header(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        return style;
    }

    public static Style footer(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        return style;
    }

    public static Style summary(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        return style;
    }

    public static Style columnHeader() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBgColor("#a1a1a1");
        style.getTextFormat().setBold(true);
        style.setBorderRight(new Border());
        style.setBorderLeft(new Border());
        style.setBorderTop(new Border());
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style column(boolean rtl, Class<?> type) {
        Style style = new Style();
        if (type == boolean.class || type == Boolean.class || Temporal.class.isAssignableFrom(type)) {
            style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        } else if (Number.class.isAssignableFrom(type)
                || byte.class == type || short.class == type
                || int.class == type || long.class == type
                || float.class == type || double.class == type) {
            style.setHorizontalAlign(HorizontalTextAlignment.RIGHT);
        } else {
            style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        }
        style.setBorderRight(new Border());
        style.setBorderLeft(new Border());
        style.setBorderTop(new Border());
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style dateTime(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        return style;
    }

    public static Style username(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        return style;
    }

    public static Style pageNumber() {
        Style style = new Style();
        style.setBorderTop(new Border());
        return style;
    }

    public static Style titleSubtotal() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderTop(new Border());
        return style;
    }

    public static Style titleSubtotalLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderBottom(new Border());
        style.getTextFormat().setBold(true);
        return style;
    }

    public static Style pageHeaderSubtotal() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderTop(new Border());
        return style;
    }

    public static Style pageHeaderSubtotalLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.getTextFormat().setBold(true);
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style pageFooterSubtotal() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderTop(new Border());
        return style;
    }

    public static Style pageFooterSubtotalLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.getTextFormat().setBold(true);
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style columnHeaderSubtotal() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBgColor("#c9c9c9");
        style.setBorderRight(new Border());
        style.setBorderLeft(new Border());
        style.setBorderTop(new Border());
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style columnHeaderSubtotalLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBgColor("#c9c9c9");
        style.getTextFormat().setBold(true);
        style.setBorderRight(new Border());
        style.setBorderLeft(new Border());
        style.setBorderTop(new Border());
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style columnFooterSubtotal() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderTop(new Border());
        return style;
    }

    public static Style columnFooterSubtotalLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderBottom(new Border());
        style.getTextFormat().setBold(true);
        return style;
    }

    public static Style summarySubtotal() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBorderTop(new Border());
        return style;
    }

    public static Style summarySubtotalLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.getTextFormat().setBold(true);
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style group(boolean rtl) {
        Style style = new Style();
        style.setHorizontalAlign(rtl ? HorizontalTextAlignment.RIGHT : HorizontalTextAlignment.LEFT);
        style.setBorderRight(new Border());
        style.setBorderLeft(new Border());
        style.setBorderTop(new Border());
        style.setBorderBottom(new Border());
        return style;
    }

    public static Style groupLabel() {
        Style style = new Style();
        style.setHorizontalAlign(HorizontalTextAlignment.CENTER);
        style.setBgColor("#c9c9c9");
        style.getTextFormat().setBold(true);
        style.setBorderRight(new Border());
        style.setBorderLeft(new Border());
        style.setBorderTop(new Border());
        style.setBorderBottom(new Border());
        return style;
    }

}
