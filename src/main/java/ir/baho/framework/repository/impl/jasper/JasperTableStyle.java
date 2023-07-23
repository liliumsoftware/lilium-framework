package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.export.LengthUtil;
import net.sf.jasperreports.engine.export.oasis.TableStyle;
import net.sf.jasperreports.engine.export.oasis.WriterHelper;
import net.sf.jasperreports.engine.util.JRColorUtil;

import java.awt.Color;

public class JasperTableStyle extends TableStyle {

    private final int width;
    private final int pageFormatIndex;
    private final boolean isFrame;
    private final boolean isPageBreak;
    private final Color tabColor;
    private final boolean rtl;

    public JasperTableStyle(WriterHelper styleWriter, int width, int pageFormatIndex, boolean isFrame, boolean isPageBreak, Color tabColor, boolean rtl) {
        super(styleWriter, width, pageFormatIndex, isFrame, isPageBreak, tabColor);
        this.width = width;
        this.pageFormatIndex = pageFormatIndex;
        this.isFrame = isFrame;
        this.isPageBreak = isPageBreak;
        this.tabColor = tabColor;
        this.rtl = rtl;
    }

    @Override
    public String getId() {
        return width + "|" + pageFormatIndex + "|" + isFrame + "|" + isPageBreak + "|" + tabColor;
    }

    @Override
    public void write(String tableStyleName) {
        styleWriter.write(" <style:style style:name=\"" + tableStyleName + "\"");
        if (!isFrame) {
            styleWriter.write(" style:master-page-name=\"master_" + pageFormatIndex + "\"");
        }
        styleWriter.write(" style:family=\"table\">\n");
        styleWriter.write("   <style:table-properties");
        if (rtl) {
            styleWriter.write(" style:writing-mode=\"rl-tb\"");
        }
        styleWriter.write(" table:align=\"left\" style:width=\"" + LengthUtil.inchFloor4Dec(width) + "in\"");
        if (isPageBreak) {
            styleWriter.write(" fo:break-before=\"page\"");
        }
        if (tabColor != null) {
            styleWriter.write(" tableooo:tab-color=\"#" + JRColorUtil.getColorHexa(tabColor) + "\"");
        }
        styleWriter.write("/>\n");
        styleWriter.write(" </style:style>\n");
        styleWriter.flush();
    }

}
