package ir.baho.framework.repository.impl.jasper;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.export.LengthUtil;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.PptxRunHelper;
import net.sf.jasperreports.engine.export.ooxml.PptxSlideHelper;
import net.sf.jasperreports.engine.export.ooxml.PptxSlideRelsHelper;
import net.sf.jasperreports.engine.export.ooxml.type.PptxFieldTypeEnum;
import net.sf.jasperreports.engine.export.zip.ExportZipEntry;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.JRStyledTextUtil;
import net.sf.jasperreports.engine.util.StyledTextWriteContext;

import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class JasperPptxExporter extends JRPptxExporter {

    private final boolean rtl;
    private PptxRunHelper runHelper;

    @Override
    protected void createSlideMaster() {
        ExportZipEntry slideMasterRelsEntry = pptxZip.addSlideMasterRels();
        Writer slideMasterRelsWriter = slideMasterRelsEntry.getWriter();
        slideRelsHelper = new PptxSlideRelsHelper(jasperReportsContext, slideMasterRelsWriter);

        ExportZipEntry slideMasterEntry = pptxZip.addSlideMaster();
        Writer slideMasterWriter = slideMasterEntry.getWriter();
        slideHelper = new PptxSlideHelper(jasperReportsContext, slideMasterWriter, slideRelsHelper);

        runHelper = new PptxRunHelper(jasperReportsContext, slideMasterWriter, fontHelper);

        slideHelper.exportHeader(true, false);
        slideRelsHelper.exportHeader(true);
    }

    @Override
    protected void createSlide(boolean hideSlideMaster) {
        presentationHelper.exportSlide(slideIndex + 1);
        ctHelper.exportSlide(slideIndex + 1);
        presentationRelsHelper.exportSlide(slideIndex + 1);

        ExportZipEntry slideRelsEntry = pptxZip.addSlideRels(slideIndex + 1);
        Writer slideRelsWriter = slideRelsEntry.getWriter();
        slideRelsHelper = new PptxSlideRelsHelper(jasperReportsContext, slideRelsWriter);

        ExportZipEntry slideEntry = pptxZip.addSlide(slideIndex + 1);
        Writer slideWriter = slideEntry.getWriter();
        slideHelper = new PptxSlideHelper(jasperReportsContext, slideWriter, slideRelsHelper);

        runHelper = new PptxRunHelper(jasperReportsContext, slideWriter, fontHelper);

        slideHelper.exportHeader(false, hideSlideMaster);
        slideRelsHelper.exportHeader(false);
    }

    @Override
    public void exportText(JRPrintText text) {
        JRStyledText styledText = getStyledText(text);

        int textLength = 0;

        if (styledText != null) {
            textLength = styledText.length();
        }

        int x;
        int y;
        int width;
        int height;
        int rotation;

        int leftPadding = text.getLineBox().getLeftPadding();
        int topPadding = text.getLineBox().getTopPadding();
        int rightPadding = text.getLineBox().getRightPadding();
        int bottomPadding = text.getLineBox().getBottomPadding();

        switch (text.getRotationValue()) {
            case LEFT -> {
                rotation = -5400000;
                x = text.getX() + getOffsetX() - (text.getHeight() - text.getWidth()) / 2;
                y = text.getY() + getOffsetY() + (text.getHeight() - text.getWidth()) / 2;
                width = text.getHeight();
                height = text.getWidth();
                int tmpPadding = topPadding;
                topPadding = leftPadding;
                leftPadding = bottomPadding;
                bottomPadding = rightPadding;
                rightPadding = tmpPadding;
            }
            case RIGHT -> {
                rotation = 5400000;
                x = text.getX() + getOffsetX() - (text.getHeight() - text.getWidth()) / 2;
                y = text.getY() + getOffsetY() + (text.getHeight() - text.getWidth()) / 2;
                width = text.getHeight();
                height = text.getWidth();
                int tmpPadding = topPadding;
                topPadding = rightPadding;
                rightPadding = bottomPadding;
                bottomPadding = leftPadding;
                leftPadding = tmpPadding;
            }
            case UPSIDE_DOWN -> {
                rotation = 10800000;
                x = text.getX() + getOffsetX();
                y = text.getY() + getOffsetY();
                width = text.getWidth();
                height = text.getHeight();
                int tmpPadding = topPadding;
                topPadding = bottomPadding;
                bottomPadding = tmpPadding;
                tmpPadding = leftPadding;
                leftPadding = rightPadding;
                rightPadding = tmpPadding;
            }
            default -> {
                rotation = 0;
                x = text.getX() + getOffsetX();
                y = text.getY() + getOffsetY();
                width = text.getWidth();
                height = text.getHeight();
            }
        }

        slideHelper.write("<p:sp>\n");
        slideHelper.write("  <p:nvSpPr>\n");
        slideHelper.write("    <p:cNvPr id=\"" + toOOXMLId(text) + "\" name=\"Text\">\n");

        String href = getHyperlinkURL(text);
        if (href != null) {
            slideHelper.exportHyperlink(href);
        }

        slideHelper.write("    </p:cNvPr>\n");
        slideHelper.write("    <p:cNvSpPr>\n");
        slideHelper.write("      <a:spLocks noGrp=\"1\"/>\n");
        slideHelper.write("    </p:cNvSpPr>\n");
        slideHelper.write("    <p:nvPr/>\n");
        slideHelper.write("  </p:nvSpPr>\n");
        slideHelper.write("  <p:spPr>\n");
        slideHelper.write("    <a:xfrm rot=\"" + rotation + "\">\n");
        slideHelper.write("      <a:off x=\"" + LengthUtil.emu(x) + "\" y=\"" + LengthUtil.emu(y) + "\"/>\n");
        slideHelper.write("      <a:ext cx=\"" + LengthUtil.emu(width) + "\" cy=\"" + LengthUtil.emu(height) + "\"/>\n");
        slideHelper.write("    </a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>\n");
        if (text.getModeValue() == ModeEnum.OPAQUE && text.getBackcolor() != null) {
            slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(text.getBackcolor()) + "\"/></a:solidFill>\n");
        }

        exportPen(text.getLineBox());

        slideHelper.write("  </p:spPr>\n");
        slideHelper.write("  <p:txBody>\n");
        slideHelper.write("    <a:bodyPr wrap=\"square\" lIns=\"" +
                LengthUtil.emu(leftPadding) +
                "\" tIns=\"" +
                LengthUtil.emu(topPadding) +
                "\" rIns=\"" +
                LengthUtil.emu(rightPadding) +
                "\" bIns=\"" +
                LengthUtil.emu(bottomPadding) +
                "\" rtlCol=\"0\" anchor=\"");
        switch (text.getVerticalTextAlign()) {
            case BOTTOM -> slideHelper.write("b");
            case MIDDLE -> slideHelper.write("ctr");
            default -> slideHelper.write("t");
        }
        slideHelper.write("\"/>\n");
        slideHelper.write("    <a:lstStyle/>\n");

        slideHelper.write("    <a:p>\n");

        slideHelper.write("<a:pPr");
        slideHelper.write(" algn=\"");
        switch (text.getHorizontalTextAlign()) {
            case CENTER -> slideHelper.write("ctr");
            case RIGHT -> slideHelper.write("r");
            case JUSTIFIED -> slideHelper.write("just");
            default -> slideHelper.write("l");
        }
        if (rtl) {
            slideHelper.write("\" rtl=\"1");
        }
        slideHelper.write("\">\n");
        slideHelper.write("<a:lnSpc><a:spcPct");
        slideHelper.write(" val=\"");
        switch (text.getParagraph().getLineSpacing()) {
            case DOUBLE -> slideHelper.write("200");
            case ONE_AND_HALF -> slideHelper.write("150");
            default -> slideHelper.write("100");
        }
        slideHelper.write("%\"/></a:lnSpc>\n");
        runHelper.exportProps(text, getTextLocale(text));
        slideHelper.write("</a:pPr>\n");

        if (textLength > 0) {
            PptxFieldTypeEnum fieldTypeEnum = PptxFieldTypeEnum.getByName(JRPropertiesUtil.getOwnProperty(text, PROPERTY_FIELD_TYPE));
            String uuid = null;
            String fieldType = null;
            if (fieldTypeEnum != null) {
                uuid = text.getUUID().toString().toUpperCase();
                fieldType = fieldTypeEnum.getName();
            }
            exportStyledText(text.getStyle(), styledText, getTextLocale(text), fieldType, uuid);
        }

        slideHelper.write("    </a:p>\n");

        slideHelper.write("  </p:txBody>\n");
        slideHelper.write("</p:sp>\n");
    }

    @Override
    protected void exportStyledText(JRStyle style, JRStyledText styledText, Locale locale, String fieldType, String uuid) {
        StyledTextWriteContext context = new StyledTextWriteContext();

        String allText = styledText.getText();

        int runLimit = 0;

        AttributedCharacterIterator iterator = styledText.getAttributedString().getIterator();

        while (runLimit < styledText.length() && (runLimit = iterator.getRunLimit()) <= styledText.length()) {
            Map<AttributedCharacterIterator.Attribute, Object> attributes = iterator.getAttributes();

            String runText = allText.substring(iterator.getIndex(), runLimit);

            context.next(attributes, runText);

            if (!runText.isEmpty()) {
                String bulletText = JRStyledTextUtil.getIndentedBulletText(context);

                String text = (bulletText == null ? "" : bulletText) + runText;

                if (fieldType != null) {
                    runHelper.export(style, attributes, text, locale, invalidCharReplacement, fieldType, uuid);
                } else {
                    runHelper.export(style, attributes, text, locale, invalidCharReplacement);
                }
            }

            iterator.setIndex(runLimit);
        }
    }

}
