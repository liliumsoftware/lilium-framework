package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JRTextAlignment;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.oasis.CellStyle;
import net.sf.jasperreports.engine.export.oasis.ParagraphStyle;
import net.sf.jasperreports.engine.export.oasis.WriterHelper;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.RotationEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;

public class JasperCellStyle extends CellStyle {

    private final String horizontalAlignment;
    private final String verticalAlignment;
    private final boolean shrinkToFit;
    private final boolean wrapText;
    private final boolean rtl;
    private String backcolor;

    public JasperCellStyle(WriterHelper styleWriter, JRExporterGridCell gridCell, boolean shrinkToFit, boolean wrapText, boolean rtl) {
        super(styleWriter, gridCell, shrinkToFit, wrapText);
        this.rtl = rtl;

        JRPrintElement element = gridCell.getElement();

        if (element != null && element.getModeValue() == ModeEnum.OPAQUE) {
            backcolor = JRColorUtil.getColorHexa(element.getBackcolor());
        } else {
            if (gridCell.getBackcolor() != null) {
                backcolor = JRColorUtil.getColorHexa(gridCell.getBackcolor());
            }
        }

        RotationEnum rotation = element instanceof JRPrintText ? ((JRPrintText) element).getRotationValue() : RotationEnum.NONE;
        VerticalTextAlignEnum vAlign = VerticalTextAlignEnum.TOP;
        HorizontalTextAlignEnum hAlign = HorizontalTextAlignEnum.LEFT;

        JRTextAlignment alignment = element instanceof JRTextAlignment ? (JRTextAlignment) element : null;
        if (alignment != null) {
            vAlign = alignment.getVerticalTextAlign();
            hAlign = alignment.getHorizontalTextAlign();
        }

        horizontalAlignment = ParagraphStyle.getHorizontalAlignment(hAlign, vAlign, rotation);
        verticalAlignment = ParagraphStyle.getVerticalAlignment(hAlign, vAlign, rotation);
        this.shrinkToFit = shrinkToFit;
        this.wrapText = wrapText;
        setBox(gridCell.getBox());
    }

    @Override
    public String getId() {
        return backcolor + super.getId() + "|" + horizontalAlignment + "|" + verticalAlignment + "|" + shrinkToFit + "|" + wrapText;
    }

    @Override
    public void write(String cellStyleName) {
        styleWriter.write("<style:style style:name=\"");
        styleWriter.write(cellStyleName);
        styleWriter.write("\"");
        styleWriter.write(" style:family=\"table-cell\">\n");
        styleWriter.write(" <style:table-cell-properties");
        styleWriter.write(" style:shrink-to-fit=\"" + shrinkToFit + "\"");
        styleWriter.write(" fo:wrap-option=\"" + (!wrapText || shrinkToFit ? "no-" : "") + "wrap\"");
        if (backcolor != null) {
            styleWriter.write(" fo:background-color=\"#");
            styleWriter.write(backcolor);
            styleWriter.write("\"");
        }

        writeBorder(TOP_BORDER);
        writeBorder(LEFT_BORDER);
        writeBorder(BOTTOM_BORDER);
        writeBorder(RIGHT_BORDER);

        if (verticalAlignment != null) {
            styleWriter.write(" style:vertical-align=\"");
            styleWriter.write(verticalAlignment);
            styleWriter.write("\"");
        }

        styleWriter.write("/>\n");

        if (horizontalAlignment != null) {
            styleWriter.write(" <style:paragraph-properties");
            if (rtl) {
                styleWriter.write(" style:writing-mode=\"rl-tb\"");
            }
            styleWriter.write(" fo:text-align=\"");
            styleWriter.write(horizontalAlignment);
            styleWriter.write("\"");
            styleWriter.write("/>\n");
        }

        styleWriter.write("</style:style>\n");
    }

}
