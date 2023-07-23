package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JRPrintGraphicElement;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.oasis.CellStyle;
import net.sf.jasperreports.engine.export.oasis.GraphicStyle;
import net.sf.jasperreports.engine.export.oasis.StyleCache;
import net.sf.jasperreports.engine.export.oasis.TableStyle;
import net.sf.jasperreports.engine.export.oasis.WriterHelper;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JasperStyleCache extends StyleCache {

    private final WriterHelper styleWriter;
    private final Map<String, String> tableStyles = new HashMap<>();
    private final Map<String, String> cellStyles = new HashMap<>();
    private final boolean rtl;
    private int tableStylesCounter;
    private int cellStylesCounter;
    private int graphicStylesCounter;

    public JasperStyleCache(JasperReportsContext jasperReportsContext, WriterHelper styleWriter, String exporterKey, boolean rtl) {
        super(jasperReportsContext, styleWriter, exporterKey);
        this.styleWriter = styleWriter;
        this.rtl = rtl;
    }

    @Override
    public String getGraphicStyle(JRPrintGraphicElement element, double cropTop, double cropLeft, double cropBottom, double cropRight) {
        GraphicStyle graphicStyle = new GraphicStyle(styleWriter, element, cropTop, cropLeft, cropBottom, cropRight);

        String graphicStyleId = graphicStyle.getId();
        String graphicStyleName = cellStyles.get(graphicStyleId);

        if (graphicStyleName == null) {
            graphicStyleName = "G" + graphicStylesCounter++;

            graphicStyle.write(graphicStyleName);
        }

        return graphicStyleName;
    }

    @Override
    public String getCellStyle(JRExporterGridCell gridCell, boolean shrinkToFit, boolean wrapText) {
        CellStyle cellStyle = new JasperCellStyle(styleWriter, gridCell, shrinkToFit, wrapText, rtl);

        String cellStyleId = cellStyle.getId();
        String cellStyleName = cellStyles.get(cellStyleId);

        if (cellStyleName == null) {
            cellStyleName = "C" + cellStylesCounter++;
            cellStyles.put(cellStyleId, cellStyleName);

            cellStyle.write(cellStyleName);
        }

        return cellStyleName;
    }

    @Override
    public String getTableStyle(int width, int pageFormatIndex, boolean isFrame, boolean isPageBreak, Color tabColor) throws IOException {
        TableStyle tableStyle = new JasperTableStyle(styleWriter, width, pageFormatIndex, isFrame, isPageBreak, tabColor, rtl);

        String tableStyleId = tableStyle.getId();
        String tableStyleName = tableStyles.get(tableStyleId);

        if (tableStyleName == null) {
            tableStyleName = "TBL" + tableStylesCounter++;
            tableStyles.put(tableStyleId, tableStyleName);

            tableStyle.write(tableStyleName);
        }

        return tableStyleName;
    }

}
