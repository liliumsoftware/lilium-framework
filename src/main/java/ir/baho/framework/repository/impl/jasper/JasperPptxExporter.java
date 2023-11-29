package ir.baho.framework.repository.impl.jasper;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPen;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintElementIndex;
import net.sf.jasperreports.engine.JRPrintEllipse;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JRPrintLine;
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
import net.sf.jasperreports.engine.export.zip.FileBufferedZipEntry;
import net.sf.jasperreports.engine.type.LineDirectionEnum;
import net.sf.jasperreports.engine.type.LineStyleEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.RotationEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.util.ExifOrientationEnum;
import net.sf.jasperreports.engine.util.ImageUtil;
import net.sf.jasperreports.engine.util.JRColorUtil;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.JRStyledTextUtil;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.engine.util.Pair;
import net.sf.jasperreports.engine.util.StyledTextWriteContext;
import net.sf.jasperreports.renderers.DataRenderable;
import net.sf.jasperreports.renderers.DimensionRenderable;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.RenderersCache;
import net.sf.jasperreports.renderers.ResourceRenderer;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class JasperPptxExporter extends JRPptxExporter {

    private final boolean rtl;

    protected PptxSlideHelper slideHelper;
    protected PptxSlideRelsHelper slideRelsHelper;
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
    protected void closeSlideMaster() {
        if (slideHelper != null) {
            slideHelper.exportFooter(true);

            slideHelper.close();

            slideRelsHelper.exportFooter();

            slideRelsHelper.close();
        }
    }

    @Override
    protected void closeSlide() {
        if (slideHelper != null) {
            slideHelper.exportFooter(false);

            slideHelper.close();

            slideRelsHelper.exportFooter();

            slideRelsHelper.close();
        }
    }

    @Override
    protected void exportLine(JRPrintLine line) {
        int x = line.getX() + getOffsetX();
        int y = line.getY() + getOffsetY();
        int height = line.getHeight();
        int width = line.getWidth();

        if (width <= 1 || height <= 1) {
            if (width > 1) {
                height = 0;
            } else {
                width = 0;
            }
        }

        slideHelper.write("<p:sp>\n");
        slideHelper.write("  <p:nvSpPr>\n");
        slideHelper.write("    <p:cNvPr id=\"" + toOOXMLId(line) + "\" name=\"Line\"/>\n");
        slideHelper.write("    <p:cNvSpPr>\n");
        slideHelper.write("      <a:spLocks noGrp=\"1\"/>\n");
        slideHelper.write("    </p:cNvSpPr>\n");
        slideHelper.write("    <p:nvPr/>\n");
        slideHelper.write("  </p:nvSpPr>\n");
        slideHelper.write("  <p:spPr>\n");
        slideHelper.write("    <a:xfrm" + (line.getDirectionValue() == LineDirectionEnum.BOTTOM_UP ? " flipV=\"1\"" : "") + ">\n");
        slideHelper.write("      <a:off x=\"" + LengthUtil.emu(x) + "\" y=\"" + LengthUtil.emu(y) + "\"/>\n");
        slideHelper.write("      <a:ext cx=\"" + LengthUtil.emu(width) + "\" cy=\"" + LengthUtil.emu(height) + "\"/>\n");
        slideHelper.write("    </a:xfrm><a:prstGeom prst=\"line\"><a:avLst/></a:prstGeom>\n");
        if (line.getModeValue() == ModeEnum.OPAQUE && line.getBackcolor() != null) {
            slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(line.getBackcolor()) + "\"/></a:solidFill>\n");
        }

        exportPen(line.getLinePen());

        slideHelper.write("  </p:spPr>\n");
        slideHelper.write("  <p:txBody>\n");
        slideHelper.write("    <a:bodyPr rtlCol=\"0\" anchor=\"ctr\"/>\n");
        slideHelper.write("    <a:lstStyle/>\n");
        slideHelper.write("    <a:p>\n");
        slideHelper.write("<a:pPr algn=\"ctr\"/>\n");
        slideHelper.write("    </a:p>\n");
        slideHelper.write("  </p:txBody>\n");
        slideHelper.write("</p:sp>\n");
    }

    @Override
    protected void exportPen(JRPen pen) {
        if (pen != null && pen.getLineWidth() > 0) {
            slideHelper.write("  <a:ln w=\"" + LengthUtil.emu(pen.getLineWidth()) + "\"");
            if (LineStyleEnum.DOUBLE.equals(pen.getLineStyleValue())) {
                slideHelper.write(" cmpd=\"dbl\"");
            }
            slideHelper.write(">\n");
            slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(pen.getLineColor()) + "\"/></a:solidFill>\n");
            slideHelper.write("<a:prstDash val=\"");
            switch (pen.getLineStyleValue()) {
                case DASHED -> slideHelper.write("dash");
                case DOTTED -> slideHelper.write("dot");
                default -> slideHelper.write("solid");
            }
            slideHelper.write("\"/>\n");
            slideHelper.write("  </a:ln>\n");
        }
    }

    @Override
    protected void exportRectangle(JRPrintElement rectangle, JRPen pen, int radius) {
        slideHelper.write("<p:sp>\n");
        slideHelper.write("  <p:nvSpPr>\n");
        slideHelper.write("    <p:cNvPr id=\"" + toOOXMLId(rectangle) + "\" name=\"Rectangle\"/>\n");
        slideHelper.write("    <p:cNvSpPr>\n");
        slideHelper.write("      <a:spLocks noGrp=\"1\"/>\n");
        slideHelper.write("    </p:cNvSpPr>\n");
        slideHelper.write("    <p:nvPr/>\n");
        slideHelper.write("  </p:nvSpPr>\n");
        slideHelper.write("  <p:spPr>\n");
        slideHelper.write("    <a:xfrm>\n");
        slideHelper.write("      <a:off x=\"" + LengthUtil.emu(rectangle.getX() + getOffsetX()) + "\" y=\"" + LengthUtil.emu(rectangle.getY() + getOffsetY()) + "\"/>\n");
        slideHelper.write("      <a:ext cx=\"" + LengthUtil.emu(rectangle.getWidth()) + "\" cy=\"" + LengthUtil.emu(rectangle.getHeight()) + "\"/>\n");
        slideHelper.write("    </a:xfrm><a:prstGeom prst=\"" + (radius == 0 ? "rect" : "roundRect") + "\">");
        if (radius > 0) {
            int size = Math.min(50000, (radius * 100000) / Math.min(rectangle.getHeight(), rectangle.getWidth()));
            slideHelper.write("<a:avLst><a:gd name=\"adj\" fmla=\"val " + size + "\"/></a:avLst></a:prstGeom>\n");
        } else {
            slideHelper.write("<a:avLst/></a:prstGeom>\n");
        }
        if (rectangle.getModeValue() == ModeEnum.OPAQUE && rectangle.getBackcolor() != null) {
            slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(rectangle.getBackcolor()) + "\"/></a:solidFill>\n");
        }

        exportPen(pen);

        slideHelper.write("  </p:spPr>\n");
        slideHelper.write("  <p:txBody>\n");
        slideHelper.write("    <a:bodyPr rtlCol=\"0\" anchor=\"ctr\"/>\n");
        slideHelper.write("    <a:lstStyle/>\n");
        slideHelper.write("    <a:p>\n");
        slideHelper.write("<a:pPr algn=\"ctr\"/>\n");
        slideHelper.write("    </a:p>\n");
        slideHelper.write("  </p:txBody>\n");
        slideHelper.write("</p:sp>\n");
    }

    @Override
    protected void exportEllipse(JRPrintEllipse ellipse) {
        slideHelper.write("<p:sp>\n");
        slideHelper.write("  <p:nvSpPr>\n");
        slideHelper.write("    <p:cNvPr id=\"" + toOOXMLId(ellipse) + "\" name=\"Ellipse\"/>\n");
        slideHelper.write("    <p:cNvSpPr>\n");
        slideHelper.write("      <a:spLocks noGrp=\"1\"/>\n");
        slideHelper.write("    </p:cNvSpPr>\n");
        slideHelper.write("    <p:nvPr/>\n");
        slideHelper.write("  </p:nvSpPr>\n");
        slideHelper.write("  <p:spPr>\n");
        slideHelper.write("    <a:xfrm>\n");
        slideHelper.write("      <a:off x=\"" + LengthUtil.emu(ellipse.getX() + getOffsetX()) + "\" y=\"" + LengthUtil.emu(ellipse.getY() + getOffsetY()) + "\"/>\n");
        slideHelper.write("      <a:ext cx=\"" + LengthUtil.emu(ellipse.getWidth()) + "\" cy=\"" + LengthUtil.emu(ellipse.getHeight()) + "\"/>\n");
        slideHelper.write("    </a:xfrm><a:prstGeom prst=\"ellipse\"><a:avLst/></a:prstGeom>\n");
        if (ellipse.getModeValue() == ModeEnum.OPAQUE && ellipse.getBackcolor() != null) {
            slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(ellipse.getBackcolor()) + "\"/></a:solidFill>\n");
        }

        exportPen(ellipse.getLinePen());

        slideHelper.write("  </p:spPr>\n");
        slideHelper.write("  <p:txBody>\n");
        slideHelper.write("    <a:bodyPr rtlCol=\"0\" anchor=\"ctr\"/>\n");
        slideHelper.write("    <a:lstStyle/>\n");
        slideHelper.write("    <a:p>\n");
        slideHelper.write("<a:pPr algn=\"ctr\"/>\n");
        slideHelper.write("    </a:p>\n");
        slideHelper.write("  </p:txBody>\n");
        slideHelper.write("</p:sp>\n");
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

    @Override
    public void exportImage(JRPrintImage image) throws JRException {
        int leftPadding = image.getLineBox().getLeftPadding();
        int topPadding = image.getLineBox().getTopPadding();
        int rightPadding = image.getLineBox().getRightPadding();
        int bottomPadding = image.getLineBox().getBottomPadding();

        JRPen pen = getPptxPen(image.getLineBox());

        boolean hasPadding = (leftPadding + topPadding + rightPadding + bottomPadding) > 0;
        if (hasPadding) {
            exportRectangle(image, pen, 0);
        }

        int availableImageWidth = image.getWidth() - leftPadding - rightPadding;
        availableImageWidth = Math.max(availableImageWidth, 0);

        int availableImageHeight = image.getHeight() - topPadding - bottomPadding;
        availableImageHeight = Math.max(availableImageHeight, 0);

        Renderable renderer = image.getRenderer();

        if (renderer != null && availableImageWidth > 0 && availableImageHeight > 0) {
            InternalImageProcessor imageProcessor = new InternalImageProcessor(image, availableImageWidth, availableImageHeight);

            InternalImageProcessorResult imageProcessorResult = null;

            try {
                imageProcessorResult = imageProcessor.process(renderer);
            } catch (Exception e) {
                Renderable onErrorRenderer = getRendererUtil().handleImageError(e, image.getOnErrorTypeValue());
                if (onErrorRenderer != null) {
                    imageProcessorResult = imageProcessor.process(onErrorRenderer);
                }
            }

            if (imageProcessorResult != null) {
                int renderWidth = availableImageWidth;
                int renderHeight = availableImageHeight;

                int xoffset = 0;
                int yoffset = 0;

                double cropTop = 0;
                double cropLeft = 0;
                double cropBottom = 0;
                double cropRight = 0;

                int angle = 0;

                switch (image.getScaleImageValue()) {
                    case FILL_FRAME -> {
                        switch (ImageUtil.getRotation(image.getRotation(), imageProcessorResult.exifOrientation)) {
                            case LEFT -> {
                                renderWidth = availableImageHeight;
                                renderHeight = availableImageWidth;
                                xoffset = (availableImageWidth - availableImageHeight) / 2;
                                yoffset = -(availableImageWidth - availableImageHeight) / 2;
                                angle = -90;
                            }
                            case RIGHT -> {
                                renderWidth = availableImageHeight;
                                renderHeight = availableImageWidth;
                                xoffset = (availableImageWidth - availableImageHeight) / 2;
                                yoffset = -(availableImageWidth - availableImageHeight) / 2;
                                angle = 90;
                            }
                            case UPSIDE_DOWN -> angle = 180;
                            default -> {
                            }
                        }
                    }
                    case CLIP -> {
                        double normalWidth = availableImageWidth;
                        double normalHeight = availableImageHeight;

                        Dimension2D dimension = imageProcessorResult.dimension;
                        if (dimension != null) {
                            normalWidth = dimension.getWidth();
                            normalHeight = dimension.getHeight();
                        }

                        switch (ImageUtil.getRotation(image.getRotation(), imageProcessorResult.exifOrientation)) {
                            case LEFT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                renderWidth = availableImageHeight;
                                renderHeight = availableImageWidth;
                                xoffset = (availableImageWidth - availableImageHeight) / 2;
                                yoffset = -(availableImageWidth - availableImageHeight) / 2;
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageHeight - normalWidth) / availableImageHeight;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageHeight - normalWidth) / availableImageHeight;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageWidth - normalHeight) / availableImageWidth;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageWidth - normalHeight) / availableImageWidth;
                                angle = -90;
                            }
                            case RIGHT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                renderWidth = availableImageHeight;
                                renderHeight = availableImageWidth;
                                xoffset = (availableImageWidth - availableImageHeight) / 2;
                                yoffset = -(availableImageWidth - availableImageHeight) / 2;
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageHeight - normalWidth) / availableImageHeight;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageHeight - normalWidth) / availableImageHeight;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageWidth - normalHeight) / availableImageWidth;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageWidth - normalHeight) / availableImageWidth;
                                angle = 90;
                            }
                            case UPSIDE_DOWN -> {
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageWidth - normalWidth) / availableImageWidth;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageWidth - normalWidth) / availableImageWidth;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageHeight - normalHeight) / availableImageHeight;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageHeight - normalHeight) / availableImageHeight;
                                angle = 180;
                            }
                            default -> {
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageWidth - normalWidth) / availableImageWidth;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageWidth - normalWidth) / availableImageWidth;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageHeight - normalHeight) / availableImageHeight;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageHeight - normalHeight) / availableImageHeight;
                            }
                        }

                        ImageUtil.Insets exifCrop = ImageUtil.getExifCrop(image, imageProcessorResult.exifOrientation, cropTop, cropLeft, cropBottom, cropRight);
                        cropLeft = exifCrop.left;
                        cropRight = exifCrop.right;
                        cropTop = exifCrop.top;
                        cropBottom = exifCrop.bottom;
                    }
                    default -> {
                        double normalWidth = availableImageWidth;
                        double normalHeight = availableImageHeight;

                        Dimension2D dimension = imageProcessorResult.dimension;
                        if (dimension != null) {
                            normalWidth = dimension.getWidth();
                            normalHeight = dimension.getHeight();
                        }

                        double ratioX;
                        double ratioY;

                        double imageWidth;
                        double imageHeight;

                        switch (ImageUtil.getRotation(image.getRotation(), imageProcessorResult.exifOrientation)) {
                            case LEFT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                renderWidth = availableImageHeight;
                                renderHeight = availableImageWidth;
                                ratioX = availableImageWidth / normalHeight;
                                ratioY = availableImageHeight / normalWidth;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalHeight * ratioX);
                                imageHeight = (int) (normalWidth * ratioY);
                                xoffset = (availableImageWidth - availableImageHeight) / 2;
                                yoffset = -(availableImageWidth - availableImageHeight) / 2;
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageHeight - imageHeight) / availableImageHeight;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageHeight - imageHeight) / availableImageHeight;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageWidth - imageWidth) / availableImageWidth;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageWidth - imageWidth) / availableImageWidth;
                                angle = -90;
                            }
                            case RIGHT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                renderWidth = availableImageHeight;
                                renderHeight = availableImageWidth;
                                ratioX = availableImageWidth / normalHeight;
                                ratioY = availableImageHeight / normalWidth;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalHeight * ratioX);
                                imageHeight = (int) (normalWidth * ratioY);
                                xoffset = (availableImageWidth - availableImageHeight) / 2;
                                yoffset = -(availableImageWidth - availableImageHeight) / 2;
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageHeight - imageHeight) / availableImageHeight;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageHeight - imageHeight) / availableImageHeight;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageWidth - imageWidth) / availableImageWidth;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageWidth - imageWidth) / availableImageWidth;
                                angle = 90;
                            }
                            case UPSIDE_DOWN -> {
                                ratioX = availableImageWidth / normalWidth;
                                ratioY = availableImageHeight / normalHeight;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalWidth * ratioX);
                                imageHeight = (int) (normalHeight * ratioY);
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageWidth - imageWidth) / availableImageWidth;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageWidth - imageWidth) / availableImageWidth;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageHeight - imageHeight) / availableImageHeight;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageHeight - imageHeight) / availableImageHeight;
                                angle = 180;
                            }
                            default -> {
                                ratioX = availableImageWidth / normalWidth;
                                ratioY = availableImageHeight / normalHeight;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalWidth * ratioX);
                                imageHeight = (int) (normalHeight * ratioY);
                                cropLeft = ImageUtil.getXAlignFactor(image) * (availableImageWidth - imageWidth) / availableImageWidth;
                                cropRight = (1f - ImageUtil.getXAlignFactor(image)) * (availableImageWidth - imageWidth) / availableImageWidth;
                                cropTop = ImageUtil.getYAlignFactor(image) * (availableImageHeight - imageHeight) / availableImageHeight;
                                cropBottom = (1f - ImageUtil.getYAlignFactor(image)) * (availableImageHeight - imageHeight) / availableImageHeight;
                            }
                        }

                        ImageUtil.Insets exifCrop = ImageUtil.getExifCrop(image, imageProcessorResult.exifOrientation, cropTop, cropLeft, cropBottom, cropRight);
                        cropLeft = exifCrop.left;
                        cropRight = exifCrop.right;
                        cropTop = exifCrop.top;
                        cropBottom = exifCrop.bottom;
                    }
                }

                slideRelsHelper.exportImage(imageProcessorResult.imagePath);

                slideHelper.write("<p:pic>\n");
                slideHelper.write("  <p:nvPicPr>\n");
                slideHelper.write("    <p:cNvPr id=\"" + toOOXMLId(image) + "\" name=\"Picture\">\n");

                String href = getHyperlinkURL(image);
                if (href != null) {
                    slideHelper.exportHyperlink(href);
                }

                slideHelper.write("    </p:cNvPr>\n");
                slideHelper.write("    <p:cNvPicPr>\n");
                slideHelper.write("      <a:picLocks noChangeAspect=\"1\"/>\n");
                slideHelper.write("    </p:cNvPicPr>\n");
                slideHelper.write("    <p:nvPr/>\n");
                slideHelper.write("  </p:nvPicPr>\n");
                slideHelper.write("<p:blipFill>\n");
                slideHelper.write("<a:blip r:embed=\"" + imageProcessorResult.imagePath + "\"/>");
                slideHelper.write("<a:srcRect/>");
                slideHelper.write("<a:stretch><a:fillRect");
                slideHelper.write(" l=\"" + (int) (100000 * cropLeft) + "\"");
                slideHelper.write(" t=\"" + (int) (100000 * cropTop) + "\"");
                slideHelper.write(" r=\"" + (int) (100000 * cropRight) + "\"");
                slideHelper.write(" b=\"" + (int) (100000 * cropBottom) + "\"");
                slideHelper.write("/></a:stretch>\n");
                slideHelper.write("</p:blipFill>\n");
                slideHelper.write("  <p:spPr>\n");
                slideHelper.write("    <a:xfrm rot=\"" + (60000 * angle) + "\">\n");
                slideHelper.write("      <a:off x=\"" + LengthUtil.emu(image.getX() + getOffsetX() + leftPadding + xoffset) + "\" y=\"" + LengthUtil.emu(image.getY() + getOffsetY() + topPadding + yoffset) + "\"/>\n");
                slideHelper.write("      <a:ext cx=\"" + LengthUtil.emu(renderWidth) + "\" cy=\"" + LengthUtil.emu(renderHeight) + "\"/>\n");
                slideHelper.write("    </a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>\n");
                if (image.getModeValue() == ModeEnum.OPAQUE && image.getBackcolor() != null) {
                    slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(image.getBackcolor()) + "\"/></a:solidFill>\n");
                }

                if (!hasPadding) {
                    exportPen(image.getLineBox());
                }

                slideHelper.write("  </p:spPr>\n");
                slideHelper.write("  </p:pic>\n");
            }
        }
    }

    @Override
    public void exportFrame(JRPrintFrame frame) throws JRException {
        slideHelper.write("<p:sp>\n");
        slideHelper.write("  <p:nvSpPr>\n");
        slideHelper.write("    <p:cNvPr id=\"" + toOOXMLId(frame) + "\" name=\"Frame\"/>\n");
        slideHelper.write("    <p:cNvSpPr>\n");
        slideHelper.write("      <a:spLocks noGrp=\"1\"/>\n");
        slideHelper.write("    </p:cNvSpPr>\n");
        slideHelper.write("    <p:nvPr/>\n");
        slideHelper.write("  </p:nvSpPr>\n");
        slideHelper.write("  <p:spPr>\n");
        slideHelper.write("    <a:xfrm>\n");
        slideHelper.write("      <a:off x=\"" + LengthUtil.emu(frame.getX() + getOffsetX()) + "\" y=\"" + LengthUtil.emu(frame.getY() + getOffsetY()) + "\"/>\n");
        slideHelper.write("      <a:ext cx=\"" + LengthUtil.emu(frame.getWidth()) + "\" cy=\"" + LengthUtil.emu(frame.getHeight()) + "\"/>\n");
        slideHelper.write("    </a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>\n");
        if (frame.getModeValue() == ModeEnum.OPAQUE && frame.getBackcolor() != null) {
            slideHelper.write("<a:solidFill><a:srgbClr val=\"" + JRColorUtil.getColorHexa(frame.getBackcolor()) + "\"/></a:solidFill>\n");
        }

        exportPen(frame.getLineBox());

        slideHelper.write("  </p:spPr>\n");
        slideHelper.write("  <p:txBody>\n");
        slideHelper.write("    <a:bodyPr rtlCol=\"0\" anchor=\"ctr\"/>\n");
        slideHelper.write("    <a:lstStyle/>\n");
        slideHelper.write("    <a:p>\n");
        slideHelper.write("<a:pPr algn=\"ctr\"/>\n");
        slideHelper.write("    </a:p>\n");
        slideHelper.write("  </p:txBody>\n");
        slideHelper.write("</p:sp>\n");

        setFrameElementsOffset(frame, false);

        frameIndexStack.add(elementIndex);

        List<JRPrintElement> elements = frame.getElements();
        if (elements != null && !elements.isEmpty()) {
            for (int i = 0; i < elements.size(); i++) {
                JRPrintElement element = elements.get(i);

                elementIndex = i;

                if (filter == null || filter.isToExport(element)) {
                    exportElement(element);
                }
            }
        }

        frameIndexStack.remove(frameIndexStack.size() - 1);

        restoreElementOffsets();
    }

    private static class InternalImageProcessorResult {

        protected final String imagePath;
        protected final Dimension2D dimension;
        protected final ExifOrientationEnum exifOrientation;

        protected InternalImageProcessorResult(String imagePath, Dimension2D dimension, ExifOrientationEnum exifOrientation) {
            this.imagePath = imagePath;
            this.dimension = dimension;
            this.exifOrientation = exifOrientation;
        }

    }

    private class InternalImageProcessor {

        private final JRPrintElement imageElement;
        private final RenderersCache imageRenderersCache;
        private final boolean needDimension;
        private final int availableImageWidth;
        private final int availableImageHeight;

        protected InternalImageProcessor(JRPrintImage imageElement, int availableImageWidth, int availableImageHeight) {
            this.imageElement = imageElement;
            this.imageRenderersCache = imageElement.isUsingCache() ? renderersCache : new RenderersCache(getJasperReportsContext());
            this.needDimension = imageElement.getScaleImageValue() != ScaleImageEnum.FILL_FRAME;
            if (imageElement.getRotation() == RotationEnum.LEFT || imageElement.getRotation() == RotationEnum.RIGHT) {
                this.availableImageWidth = availableImageHeight;
                this.availableImageHeight = availableImageWidth;
            } else {
                this.availableImageWidth = availableImageWidth;
                this.availableImageHeight = availableImageHeight;
            }
        }

        private InternalImageProcessorResult process(Renderable renderer) throws JRException {
            if (renderer instanceof ResourceRenderer) {
                renderer = imageRenderersCache.getLoadedRenderer((ResourceRenderer) renderer);
            }

            Dimension2D dimension = null;
            if (needDimension) {
                DimensionRenderable dimensionRenderer = imageRenderersCache.getDimensionRenderable(renderer);
                dimension = dimensionRenderer == null ? null : dimensionRenderer.getDimension(jasperReportsContext);
            }

            ExifOrientationEnum exifOrientation;

            String imagePath;

            if (renderer instanceof DataRenderable && rendererToImagePathMap.containsKey(renderer.getId())) {
                Pair<String, ExifOrientationEnum> imagePair = rendererToImagePathMap.get(renderer.getId());
                imagePath = imagePair.first();
                exifOrientation = imagePair.second();
            } else {
                JRPrintElementIndex imageIndex = getElementIndex();

                DataRenderable imageRenderer = getRendererUtil().getImageDataRenderable(
                        imageRenderersCache, renderer,
                        new Dimension(availableImageWidth, availableImageHeight),
                        ModeEnum.OPAQUE == imageElement.getModeValue() ? imageElement.getBackcolor() : null
                );

                byte[] imageData = imageRenderer.getData(jasperReportsContext);
                exifOrientation = ImageUtil.getExifOrientation(imageData);
                String fileExtension = JRTypeSniffer.getImageTypeValue(imageData).getFileExtension();
                String imageName = IMAGE_NAME_PREFIX + imageIndex.toString() + (fileExtension == null ? "" : ("." + fileExtension));

                pptxZip.addEntry(new FileBufferedZipEntry("ppt/media/" + imageName, imageData));

                imagePath = imageName;

                if (imageRenderer == renderer) {
                    rendererToImagePathMap.put(renderer.getId(), new Pair<>(imagePath, exifOrientation));
                }
            }

            return new InternalImageProcessorResult(imagePath, dimension, exifOrientation);
        }

    }

}
