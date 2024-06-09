package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporter;
import net.sf.jasperreports.engine.export.ooxml.XlsxSheetHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxSheetRelsHelper;
import net.sf.jasperreports.engine.type.RunDirectionEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;
import net.sf.jasperreports.export.XlsReportConfiguration;

import java.awt.Color;
import java.io.Writer;

public class JasperXlsxSheetHelper extends XlsxSheetHelper {

    private final XlsReportConfiguration configuration;

    public JasperXlsxSheetHelper(JasperReportsContext jasperReportsContext, Writer writer, XlsxSheetRelsHelper sheetRelsHelper, XlsReportConfiguration configuration) {
        super(jasperReportsContext, writer, sheetRelsHelper, configuration);
        this.configuration = configuration;
    }

    @Override
    public void exportHeader(boolean showGridlines, int scale, int rowFreezeIndex, int columnFreezeIndex, int maxColumnFreezeIndex, JasperPrint jasperPrint, Color tabColor) {
        write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        write("<worksheet\n");
        write(" xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"\n");
        write(" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n");

        write("<sheetPr>\n");
        if (tabColor != null) {
            write("<tabColor rgb=\"FF" + JRColorUtil.getColorHexa(tabColor) + "\"/>\n");
        }
        write("<outlinePr summaryBelow=\"0\"/>\n");

        /* the scale factor takes precedence over fitWidth and fitHeight properties */
        if ((scale < 10 || scale > 400) &&
                (configuration.getFitWidth() != null || configuration.getFitHeight() != null || Boolean.TRUE == configuration.isAutoFitPageHeight())) {
            write("<pageSetUpPr fitToPage=\"1\"/>");
        }

        write("</sheetPr><dimension ref=\"A1\"/><sheetViews><sheetView workbookViewId=\"0\"");

        if (configuration.getSheetDirection() == RunDirectionEnum.RTL) {
            write(" rightToLeft=\"1\"");
        }

        if (!showGridlines) {
            write(" showGridLines=\"0\"");
        }

        if (rowFreezeIndex > 0 || columnFreezeIndex > 0) {
            write(">\n<pane" + (columnFreezeIndex > 0 ? (" xSplit=\"" + columnFreezeIndex + "\"") : "") + (rowFreezeIndex > 0 ? (" ySplit=\"" + rowFreezeIndex + "\"") : ""));
            String columnFreezeName = columnFreezeIndex < 0 ? "A" : JRXlsAbstractExporter.getColumIndexName(columnFreezeIndex, maxColumnFreezeIndex);
            write(" topLeftCell=\"" + columnFreezeName + (rowFreezeIndex + 1) + "\"");
            String activePane = (rowFreezeIndex > 0 ? "bottom" : "top") + (columnFreezeIndex > 0 ? "Right" : "Left");
            write(" activePane=\"" + activePane + "\" state=\"frozen\"/>\n");
            write("<selection pane=\"" + activePane + "\"");
            write(" activeCell=\"" + columnFreezeName + (rowFreezeIndex + 1) + "\"");
            write(" sqref=\"" + columnFreezeName + (rowFreezeIndex + 1) + "\"");
            write("/>\n");
            write("</sheetView>\n</sheetViews>\n");
        } else {
            write("/></sheetViews>\n");
        }
        write("<sheetFormatPr defaultRowHeight=\"15\"/>\n");
    }

}
