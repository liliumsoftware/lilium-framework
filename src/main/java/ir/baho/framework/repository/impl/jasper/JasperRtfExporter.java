package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JRPen;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JRPrintLine;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.TabStop;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.LengthUtil;
import net.sf.jasperreports.engine.type.ImageTypeEnum;
import net.sf.jasperreports.engine.type.LineDirectionEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.RotationEnum;
import net.sf.jasperreports.engine.type.RunDirectionEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.util.ImageUtil;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.JRStyledTextUtil;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.engine.util.StyledTextWriteContext;
import net.sf.jasperreports.renderers.DataRenderable;
import net.sf.jasperreports.renderers.DimensionRenderable;
import net.sf.jasperreports.renderers.Renderable;
import net.sf.jasperreports.renderers.RenderersCache;
import net.sf.jasperreports.renderers.ResourceRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

public class JasperRtfExporter extends JRRtfExporter {

    private static final int LINE_SPACING_FACTOR = 240;

    private int zorder = 1;

    private int getColorIndex(Color color) throws IOException {
        int colorNdx = colors.indexOf(color);
        if (colorNdx < 0) {
            colorNdx = colors.size();
            colors.add(color);
            colorWriter.write("\\red" + color.getRed() + "\\green" + color.getGreen() + "\\blue" + color.getBlue() + ";");
        }
        return colorNdx;
    }

    private void startElement(JRPrintElement element) throws IOException {
        contentWriter.write("{\\shp\\shpbxpage\\shpbypage\\shpwr5\\shpfhdr0\\shpfblwtxt0\\shpz");
        contentWriter.write(String.valueOf(zorder++));
        contentWriter.write("\\shpleft");
        contentWriter.write(String.valueOf(LengthUtil.twip(element.getX() + getOffsetX())));
        contentWriter.write("\\shpright");
        contentWriter.write(String.valueOf(LengthUtil.twip(element.getX() + getOffsetX() + element.getWidth())));
        contentWriter.write("\\shptop");
        contentWriter.write(String.valueOf(LengthUtil.twip(element.getY() + getOffsetY())));
        contentWriter.write("\\shpbottom");
        contentWriter.write(String.valueOf(LengthUtil.twip(element.getY() + getOffsetY() + element.getHeight())));

        Color bgcolor = element.getBackcolor();

        if (element.getModeValue() == ModeEnum.OPAQUE) {
            contentWriter.write("{\\sp{\\sn fFilled}{\\sv 1}}");
            contentWriter.write("{\\sp{\\sn fillColor}{\\sv ");
            contentWriter.write(String.valueOf(getColorRGB(bgcolor)));
            contentWriter.write("}}");
        } else {
            contentWriter.write("{\\sp{\\sn fFilled}{\\sv 0}}");
        }

        contentWriter.write("{\\shpinst");
    }

    private int getColorRGB(Color color) {
        return color.getRed() + 256 * color.getGreen() + 65536 * color.getBlue();
    }

    private void finishElement() throws IOException {
        contentWriter.write("}}\n");
    }

    private void exportPen(JRPen pen) throws IOException {
        contentWriter.write("{\\sp{\\sn lineColor}{\\sv ");
        contentWriter.write(String.valueOf(getColorRGB(pen.getLineColor())));
        contentWriter.write("}}");

        float lineWidth = pen.getLineWidth();

        if (lineWidth == 0f) {
            contentWriter.write("{\\sp{\\sn fLine}{\\sv 0}}");
        }

        switch (pen.getLineStyleValue()) {
            case DOUBLE -> contentWriter.write("{\\sp{\\sn lineStyle}{\\sv 1}}");
            case DOTTED -> contentWriter.write("{\\sp{\\sn lineDashing}{\\sv 2}}");
            case DASHED -> contentWriter.write("{\\sp{\\sn lineDashing}{\\sv 1}}");
            default -> {
            }
        }

        contentWriter.write("{\\sp{\\sn lineWidth}{\\sv ");
        contentWriter.write(String.valueOf(LengthUtil.emu(lineWidth)));
        contentWriter.write("}}");
    }

    private void exportPen(Color color) throws IOException {
        contentWriter.write("{\\sp{\\sn lineColor}{\\sv ");
        contentWriter.write(String.valueOf(getColorRGB(color)));
        contentWriter.write("}}");
        contentWriter.write("{\\sp{\\sn fLine}{\\sv 0}}");
        contentWriter.write("{\\sp{\\sn lineWidth}{\\sv 0}}");
    }

    @Override
    protected void exportLine(JRPrintLine line) throws IOException {
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

        contentWriter.write("{\\shp\\shpbxpage\\shpbypage\\shpwr5\\shpfhdr0\\shpz");
        contentWriter.write(String.valueOf(zorder++));
        contentWriter.write("\\shpleft");
        contentWriter.write(String.valueOf(LengthUtil.twip(x)));
        contentWriter.write("\\shpright");
        contentWriter.write(String.valueOf(LengthUtil.twip(x + width)));
        contentWriter.write("\\shptop");
        contentWriter.write(String.valueOf(LengthUtil.twip(y)));
        contentWriter.write("\\shpbottom");
        contentWriter.write(String.valueOf(LengthUtil.twip(y + height)));

        contentWriter.write("{\\shpinst");

        contentWriter.write("{\\sp{\\sn shapeType}{\\sv 20}}");

        exportPen(line.getLinePen());

        if (line.getDirectionValue() == LineDirectionEnum.TOP_DOWN) {
            contentWriter.write("{\\sp{\\sn fFlipV}{\\sv 0}}");
        } else {
            contentWriter.write("{\\sp{\\sn fFlipV}{\\sv 1}}");
        }

        contentWriter.write("}}\n");
    }

    private void exportBorder(JRPen pen, float x, float y, float width, float height) throws IOException {
        contentWriter.write("{\\shp\\shpbxpage\\shpbypage\\shpwr5\\shpfhdr0\\shpz");
        contentWriter.write(String.valueOf(zorder++));
        contentWriter.write("\\shpleft");
        contentWriter.write(String.valueOf(LengthUtil.twip(x)));
        contentWriter.write("\\shpright");
        contentWriter.write(String.valueOf(LengthUtil.twip(x + width)));
        contentWriter.write("\\shptop");
        contentWriter.write(String.valueOf(LengthUtil.twip(y)));
        contentWriter.write("\\shpbottom");
        contentWriter.write(String.valueOf(LengthUtil.twip(y + height)));

        contentWriter.write("{\\shpinst");

        contentWriter.write("{\\sp{\\sn shapeType}{\\sv 20}}");

        exportPen(pen);

        contentWriter.write("}}\n");
    }

    @Override
    public void exportText(JRPrintText text) throws IOException, JRException {
        JRStyledText styledText = getStyledText(text);
        if (styledText == null) {
            return;
        }

        int width = text.getWidth();
        int height = text.getHeight();

        int textHeight = (int) text.getTextHeight();

        if (textHeight <= 0) {
            if (height <= 0) {
                throw new JRException(EXCEPTION_MESSAGE_KEY_INVALID_TEXT_HEIGHT, (Object[]) null);
            }
            textHeight = height;
        }

        startElement(text);

        int topPadding = text.getLineBox().getTopPadding();
        int leftPadding = text.getLineBox().getLeftPadding();
        int bottomPadding = text.getLineBox().getBottomPadding();
        int rightPadding = text.getLineBox().getRightPadding();

        String rotation;

        switch (text.getRotationValue()) {
            case LEFT -> {
                switch (text.getVerticalTextAlign()) {
                    case BOTTOM -> leftPadding = Math.max(leftPadding, width - rightPadding - textHeight);
                    case MIDDLE -> leftPadding = Math.max(leftPadding, (width - rightPadding - textHeight) / 2);
                    default -> {
                    }
                }
                rotation = "{\\sp{\\sn txflTextFlow}{\\sv 2}}";
            }
            case RIGHT -> {
                switch (text.getVerticalTextAlign()) {
                    case BOTTOM -> rightPadding = Math.max(rightPadding, width - leftPadding - textHeight);
                    case MIDDLE -> rightPadding = Math.max(rightPadding, (width - leftPadding - textHeight) / 2);
                    default -> {
                    }
                }
                rotation = "{\\sp{\\sn txflTextFlow}{\\sv 3}}";
            }
            case UPSIDE_DOWN -> {
                switch (text.getVerticalTextAlign()) {
                    case TOP -> topPadding = Math.max(topPadding, height - bottomPadding - textHeight);
                    case MIDDLE -> topPadding = Math.max(topPadding, (height - bottomPadding - textHeight) / 2);
                    default -> {
                    }
                }
                rotation = "";
            }
            default -> {
                switch (text.getVerticalTextAlign()) {
                    case BOTTOM -> topPadding = Math.max(topPadding, height - bottomPadding - textHeight);
                    case MIDDLE -> topPadding = Math.max(topPadding, (height - bottomPadding - textHeight) / 2);
                    default -> {
                    }
                }
                rotation = "";
            }
        }

        contentWriter.write(rotation);
        contentWriter.write("{\\sp{\\sn dyTextTop}{\\sv ");
        contentWriter.write(String.valueOf(LengthUtil.emu(topPadding)));
        contentWriter.write("}}");
        contentWriter.write("{\\sp{\\sn dxTextLeft}{\\sv ");
        contentWriter.write(String.valueOf(LengthUtil.emu(leftPadding)));
        contentWriter.write("}}");
        contentWriter.write("{\\sp{\\sn dyTextBottom}{\\sv ");
        contentWriter.write(String.valueOf(LengthUtil.emu(bottomPadding)));
        contentWriter.write("}}");
        contentWriter.write("{\\sp{\\sn dxTextRight}{\\sv ");
        contentWriter.write(String.valueOf(LengthUtil.emu(rightPadding)));
        contentWriter.write("}}");
        contentWriter.write("{\\sp{\\sn fLine}{\\sv 0}}");
        contentWriter.write("{\\shptxt{\\pard");
        if (text.getRunDirectionValue() == RunDirectionEnum.RTL) {
            contentWriter.write("\\rtlpar");
        }
        contentWriter.write(" ");

        contentWriter.write("\\fi" + LengthUtil.twip(text.getParagraph().getFirstLineIndent()) + " ");
        contentWriter.write("\\li" + LengthUtil.twip(text.getParagraph().getLeftIndent()) + " ");
        contentWriter.write("\\ri" + LengthUtil.twip(text.getParagraph().getRightIndent()) + " ");
        contentWriter.write("\\sb" + LengthUtil.twip(text.getParagraph().getSpacingBefore()) + " ");
        contentWriter.write("\\sa" + LengthUtil.twip(text.getParagraph().getSpacingAfter()) + " ");

        TabStop[] tabStops = text.getParagraph().getTabStops();
        if (tabStops != null) {
            for (TabStop tabStop : tabStops) {
                String tabStopAlign = switch (tabStop.getAlignment()) {
                    case CENTER -> "\\tqc";
                    case RIGHT -> "\\tqr";
                    default -> "";
                };

                contentWriter.write(tabStopAlign + "\\tx" + LengthUtil.twip(tabStop.getPosition()) + " ");
            }
        }

        if (text.getRunDirectionValue() == RunDirectionEnum.RTL) {
            contentWriter.write("\\rtlch");
        }

        contentWriter.write("\\cb");
        contentWriter.write(String.valueOf(getColorIndex(text.getBackcolor())));
        contentWriter.write(" ");

        switch (text.getHorizontalTextAlign()) {
            case CENTER -> contentWriter.write("\\qc");
            case RIGHT -> contentWriter.write("\\qr");
            case JUSTIFIED -> contentWriter.write("\\qj");
            default -> contentWriter.write("\\ql");
        }

        switch (text.getParagraph().getLineSpacing()) {
            case AT_LEAST -> {
                contentWriter.write("\\sl" + LengthUtil.twip(text.getParagraph().getLineSpacingSize()));
                contentWriter.write(" \\slmult0 ");
            }
            case FIXED -> {
                contentWriter.write("\\sl-" + LengthUtil.twip(text.getParagraph().getLineSpacingSize()));
                contentWriter.write(" \\slmult0 ");
            }
            case PROPORTIONAL -> {
                contentWriter.write("\\sl" + (int) (text.getParagraph().getLineSpacingSize() * LINE_SPACING_FACTOR));
                contentWriter.write(" \\slmult1 ");
            }
            case DOUBLE -> {
                contentWriter.write("\\sl" + (int) (2f * LINE_SPACING_FACTOR));
                contentWriter.write(" \\slmult1 ");
            }
            case ONE_AND_HALF -> {
                contentWriter.write("\\sl" + (int) (1.5f * LINE_SPACING_FACTOR));
                contentWriter.write(" \\slmult1 ");
            }
            default -> {
                contentWriter.write("\\sl" + (int) (1f * LINE_SPACING_FACTOR));
                contentWriter.write(" \\slmult1 ");
            }
        }

        if (text.getAnchorName() != null) {
            writeAnchor(text.getAnchorName());
        }

        boolean startedHyperlink = exportHyperlink(text);

        StyledTextWriteContext context = new StyledTextWriteContext();

        String plainText = styledText.getText();
        int runLimit = 0;

        AttributedCharacterIterator iterator = styledText.getAttributedString().getIterator();
        while (runLimit < styledText.length() && (runLimit = iterator.getRunLimit()) <= styledText.length()) {
            Map<Attribute, Object> styledTextAttributes = iterator.getAttributes();

            String runText = plainText.substring(iterator.getIndex(), runLimit);

            context.next(styledTextAttributes, runText);

            if (!runText.isEmpty()) {
                String bulletText = JRStyledTextUtil.getIndentedBulletText(context);

                exportStyledTextRun(styledTextAttributes, (bulletText == null ? "" : bulletText) + runText, getTextLocale(text), text.getBackcolor());
            }

            iterator.setIndex(runLimit);
        }

        endHyperlink(startedHyperlink);

        contentWriter.write("\\par}}");

        finishElement();

        exportBox(text.getLineBox(), text.getX() + getOffsetX(), text.getY() + getOffsetY(), width, height);
    }

    @Override
    public void exportImage(JRPrintImage printImage) throws JRException, IOException {
        int leftPadding = printImage.getLineBox().getLeftPadding();
        int topPadding = printImage.getLineBox().getTopPadding();
        int rightPadding = printImage.getLineBox().getRightPadding();
        int bottomPadding = printImage.getLineBox().getBottomPadding();

        int availableImageWidth = printImage.getWidth() - leftPadding - rightPadding;
        availableImageWidth = Math.max(availableImageWidth, 0);

        int availableImageHeight = printImage.getHeight() - topPadding - bottomPadding;
        availableImageHeight = Math.max(availableImageHeight, 0);

        Renderable renderer = printImage.getRenderer();

        if (renderer != null && availableImageWidth > 0 && availableImageHeight > 0) {
            InternalImageProcessor imageProcessor = new InternalImageProcessor(printImage, availableImageWidth, availableImageHeight);

            InternalImageProcessorResult imageProcessorResult = null;

            try {
                imageProcessorResult = imageProcessor.process(renderer);
            } catch (Exception e) {
                Renderable onErrorRenderer = getRendererUtil().handleImageError(e, printImage.getOnErrorTypeValue());
                if (onErrorRenderer != null) {
                    imageProcessorResult = imageProcessor.process(onErrorRenderer);
                }
            }

            if (imageProcessorResult != null) {
                int imageWidth;
                int imageHeight;
                int xoffset = 0;
                int yoffset = 0;
                float cropTop = 0;
                float cropLeft = 0;
                float cropBottom = 0;
                float cropRight = 0;
                int angle = 0;

                switch (printImage.getScaleImageValue()) {
                    case CLIP -> {
                        float normalWidth = availableImageWidth;
                        float normalHeight = availableImageHeight;

                        Dimension2D dimension = imageProcessorResult.dimension;
                        if (dimension != null) {
                            normalWidth = (int) dimension.getWidth();
                            normalHeight = (int) dimension.getHeight();
                        }

                        switch (printImage.getRotation()) {
                            case LEFT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                switch (printImage.getHorizontalImageAlign()) {
                                    case RIGHT -> {
                                        cropLeft = (-availableImageHeight + normalWidth) / normalWidth;
                                        cropRight = 0;
                                    }
                                    case CENTER -> {
                                        cropLeft = (-availableImageHeight + normalWidth) / normalWidth / 2;
                                        cropRight = cropLeft;
                                    }
                                    default -> {
                                        cropLeft = 0;
                                        cropRight = (-availableImageHeight + normalWidth) / normalWidth;
                                    }
                                }
                                switch (printImage.getVerticalImageAlign()) {
                                    case TOP -> {
                                        cropTop = 0;
                                        cropBottom = (-availableImageWidth + normalHeight) / normalHeight;
                                    }
                                    case MIDDLE -> {
                                        cropTop = (-availableImageWidth + normalHeight) / normalHeight / 2;
                                        cropBottom = cropTop;
                                    }
                                    default -> {
                                        cropTop = (-availableImageWidth + normalHeight) / normalHeight;
                                        cropBottom = 0;
                                    }
                                }
                                angle = -90;
                            }
                            case RIGHT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                switch (printImage.getHorizontalImageAlign()) {
                                    case RIGHT -> {
                                        cropLeft = (-availableImageHeight + normalWidth) / normalWidth;
                                        cropRight = 0;
                                    }
                                    case CENTER -> {
                                        cropLeft = (-availableImageHeight + normalWidth) / normalWidth / 2;
                                        cropRight = cropLeft;
                                    }
                                    default -> {
                                        cropLeft = 0;
                                        cropRight = (-availableImageHeight + normalWidth) / normalWidth;
                                    }
                                }
                                switch (printImage.getVerticalImageAlign()) {
                                    case TOP -> {
                                        cropTop = 0;
                                        cropBottom = (-availableImageWidth + normalHeight) / normalHeight;
                                    }
                                    case MIDDLE -> {
                                        cropTop = (-availableImageWidth + normalHeight) / normalHeight / 2;
                                        cropBottom = cropTop;
                                    }
                                    default -> {
                                        cropTop = (-availableImageWidth + normalHeight) / normalHeight;
                                        cropBottom = 0;
                                    }
                                }
                                angle = 90;
                            }
                            case UPSIDE_DOWN -> {
                                switch (printImage.getHorizontalImageAlign()) {
                                    case RIGHT -> {
                                        cropLeft = (-availableImageWidth + normalWidth) / normalWidth;
                                        cropRight = 0;
                                    }
                                    case CENTER -> {
                                        cropLeft = (-availableImageWidth + normalWidth) / normalWidth / 2;
                                        cropRight = cropLeft;
                                    }
                                    default -> {
                                        cropLeft = 0;
                                        cropRight = (-availableImageWidth + normalWidth) / normalWidth;
                                    }
                                }
                                switch (printImage.getVerticalImageAlign()) {
                                    case TOP -> {
                                        cropTop = 0;
                                        cropBottom = (-availableImageHeight + normalHeight) / normalHeight;
                                    }
                                    case MIDDLE -> {
                                        cropTop = (-availableImageHeight + normalHeight) / normalHeight / 2;
                                        cropBottom = cropTop;
                                    }
                                    default -> {
                                        cropTop = (-availableImageHeight + normalHeight) / normalHeight;
                                        cropBottom = 0;
                                    }
                                }
                                angle = 180;
                            }
                            default -> {
                                switch (printImage.getHorizontalImageAlign()) {
                                    case RIGHT -> {
                                        cropLeft = (-availableImageWidth + normalWidth) / normalWidth;
                                        cropRight = 0;
                                    }
                                    case CENTER -> {
                                        cropLeft = (-availableImageWidth + normalWidth) / normalWidth / 2;
                                        cropRight = cropLeft;
                                    }
                                    default -> {
                                        cropLeft = 0;
                                        cropRight = (-availableImageWidth + normalWidth) / normalWidth;
                                    }
                                }
                                switch (printImage.getVerticalImageAlign()) {
                                    case TOP -> {
                                        cropTop = 0;
                                        cropBottom = (-availableImageHeight + normalHeight) / normalHeight;
                                    }
                                    case MIDDLE -> {
                                        cropTop = (-availableImageHeight + normalHeight) / normalHeight / 2;
                                        cropBottom = cropTop;
                                    }
                                    default -> {
                                        cropTop = (-availableImageHeight + normalHeight) / normalHeight;
                                        cropBottom = 0;
                                    }
                                }
                            }
                        }

                        imageWidth = availableImageWidth;
                        imageHeight = availableImageHeight;
                    }
                    case FILL_FRAME -> {
                        imageWidth = availableImageWidth;
                        imageHeight = availableImageHeight;
                        switch (printImage.getRotation()) {
                            case LEFT -> angle = -90;
                            case RIGHT -> angle = 90;
                            case UPSIDE_DOWN -> angle = 180;
                            default -> {
                            }
                        }
                    }
                    default -> {
                        float normalWidth = availableImageWidth;
                        float normalHeight = availableImageHeight;

                        Dimension2D dimension = imageProcessorResult.dimension;
                        if (dimension != null) {
                            normalWidth = (int) dimension.getWidth();
                            normalHeight = (int) dimension.getHeight();
                        }

                        float ratioX;
                        float ratioY;

                        switch (printImage.getRotation()) {
                            case LEFT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                ratioX = availableImageWidth / normalHeight;
                                ratioY = availableImageHeight / normalWidth;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalHeight * ratioX);
                                imageHeight = (int) (normalWidth * ratioY);
                                xoffset = (int) (ImageUtil.getYAlignFactor(printImage) * (availableImageWidth - imageWidth));
                                yoffset = (int) ((1f - ImageUtil.getXAlignFactor(printImage)) * (availableImageHeight - imageHeight));
                                angle = -90;
                            }
                            case RIGHT -> {
                                if (dimension == null) {
                                    normalWidth = availableImageHeight;
                                    normalHeight = availableImageWidth;
                                }
                                ratioX = availableImageWidth / normalHeight;
                                ratioY = availableImageHeight / normalWidth;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalHeight * ratioX);
                                imageHeight = (int) (normalWidth * ratioY);
                                xoffset = (int) ((1f - ImageUtil.getYAlignFactor(printImage)) * (availableImageWidth - imageWidth));
                                yoffset = (int) (ImageUtil.getXAlignFactor(printImage) * (availableImageHeight - imageHeight));
                                angle = 90;
                            }
                            case UPSIDE_DOWN -> {
                                ratioX = availableImageWidth / normalWidth;
                                ratioY = availableImageHeight / normalHeight;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalWidth * ratioX);
                                imageHeight = (int) (normalHeight * ratioY);
                                xoffset = (int) ((1f - ImageUtil.getXAlignFactor(printImage)) * (availableImageWidth - imageWidth));
                                yoffset = (int) ((1f - ImageUtil.getYAlignFactor(printImage)) * (availableImageHeight - imageHeight));
                                angle = 180;
                            }
                            default -> {
                                ratioX = availableImageWidth / normalWidth;
                                ratioY = availableImageHeight / normalHeight;
                                ratioX = Math.min(ratioX, ratioY);
                                ratioY = ratioX;
                                imageWidth = (int) (normalWidth * ratioX);
                                imageHeight = (int) (normalHeight * ratioY);
                                xoffset = (int) (ImageUtil.getXAlignFactor(printImage) * (availableImageWidth - imageWidth));
                                yoffset = (int) (ImageUtil.getYAlignFactor(printImage) * (availableImageHeight - imageHeight));
                            }
                        }
                    }
                }

                startElement(printImage);
                exportPen(printImage.getForecolor());
                finishElement();
                boolean startedHyperlink = exportHyperlink(printImage);

                contentWriter.write("{\\shp{\\*\\shpinst\\shpbxpage\\shpbypage\\shpwr5\\shpfhdr0\\shpfblwtxt0\\shpz");
                contentWriter.write(String.valueOf(zorder++));
                contentWriter.write("\\shpleft");
                contentWriter.write(String.valueOf(LengthUtil.twip(printImage.getX() + leftPadding + xoffset + getOffsetX())));
                contentWriter.write("\\shpright");
                contentWriter.write(String.valueOf(LengthUtil.twip(printImage.getX() + leftPadding + xoffset + getOffsetX() + imageWidth)));
                contentWriter.write("\\shptop");
                contentWriter.write(String.valueOf(LengthUtil.twip(printImage.getY() + topPadding + yoffset + getOffsetY())));
                contentWriter.write("\\shpbottom");
                contentWriter.write(String.valueOf(LengthUtil.twip(printImage.getY() + topPadding + yoffset + getOffsetY() + imageHeight)));
                contentWriter.write("{\\sp{\\sn shapeType}{\\sv 75}}");
                contentWriter.write("{\\sp{\\sn fFilled}{\\sv 0}}");
                contentWriter.write("{\\sp{\\sn Rotation}{\\sv " + (65536 * angle) + "}}");
                contentWriter.write("{\\sp{\\sn fLockAspectRatio}{\\sv 0}}");

                contentWriter.write("{\\sp{\\sn cropFromTop}{\\sv ");
                contentWriter.write(String.valueOf((int) (65536 * cropTop)));
                contentWriter.write("}}");
                contentWriter.write("{\\sp{\\sn cropFromLeft}{\\sv ");
                contentWriter.write(String.valueOf((int) (65536 * cropLeft)));
                contentWriter.write("}}");
                contentWriter.write("{\\sp{\\sn cropFromBottom}{\\sv ");
                contentWriter.write(String.valueOf((int) (65536 * cropBottom)));
                contentWriter.write("}}");
                contentWriter.write("{\\sp{\\sn cropFromRight}{\\sv ");
                contentWriter.write(String.valueOf((int) (65536 * cropRight)));
                contentWriter.write("}}");

                writeShapeHyperlink(printImage);

                if (printImage.getAnchorName() != null) {
                    writeAnchor(printImage.getAnchorName());
                }

                contentWriter.write("{\\sp{\\sn pib}{\\sv {\\pict");
                if (imageProcessorResult.imageType == ImageTypeEnum.JPEG) {
                    contentWriter.write("\\jpegblip");
                } else {
                    contentWriter.write("\\pngblip");
                }
                contentWriter.write("\n");

                ByteArrayInputStream bais = new ByteArrayInputStream(imageProcessorResult.imageData);

                int count = 0;
                int current;
                while ((current = bais.read()) != -1) {
                    String helperStr = Integer.toHexString(current);
                    if (helperStr.length() < 2) {
                        helperStr = "0" + helperStr;
                    }
                    contentWriter.write(helperStr);
                    count++;
                    if (count == 64) {
                        contentWriter.write("\n");
                        count = 0;
                    }
                }

                contentWriter.write("\n}}}");
                contentWriter.write("}}\n");
                endHyperlink(startedHyperlink);
            }
        }

        int x = printImage.getX() + getOffsetX();
        int y = printImage.getY() + getOffsetY();
        int width = printImage.getWidth();
        int height = printImage.getHeight();

        if (printImage.getLineBox().getTopPen().getLineWidth() <= 0f &&
                printImage.getLineBox().getLeftPen().getLineWidth() <= 0f &&
                printImage.getLineBox().getBottomPen().getLineWidth() <= 0f &&
                printImage.getLineBox().getRightPen().getLineWidth() <= 0f) {
            if (printImage.getLinePen().getLineWidth() > 0f) {
                exportPen(printImage.getLinePen(), x, y, width, height);
            }
        } else {
            exportBox(printImage.getLineBox(), x, y, width, height);
        }
    }

    private void exportBox(JRLineBox box, int x, int y, int width, int height) throws IOException {
        exportTopPen(box.getTopPen(), box.getLeftPen(), box.getRightPen(), x, y, width, height);
        exportLeftPen(box.getTopPen(), box.getLeftPen(), box.getBottomPen(), x, y, width, height);
        exportBottomPen(box.getLeftPen(), box.getBottomPen(), box.getRightPen(), x, y, width, height);
        exportRightPen(box.getTopPen(), box.getBottomPen(), box.getRightPen(), x, y, width, height);
    }

    private void exportPen(JRPen pen, int x, int y, int width, int height) throws IOException {
        exportTopPen(pen, pen, pen, x, y, width, height);
        exportLeftPen(pen, pen, pen, x, y, width, height);
        exportBottomPen(pen, pen, pen, x, y, width, height);
        exportRightPen(pen, pen, pen, x, y, width, height);
    }

    private void exportTopPen(JRPen topPen, JRPen leftPen, JRPen rightPen, int x, int y, int width, int height) throws IOException {
        if (topPen.getLineWidth() > 0f) {
            exportBorder(topPen, x - leftPen.getLineWidth() / 2, y, width + (leftPen.getLineWidth() + rightPen.getLineWidth()) / 2, 0);
        }
    }

    private void exportLeftPen(JRPen topPen, JRPen leftPen, JRPen bottomPen, int x, int y, int width, int height) throws IOException {
        if (leftPen.getLineWidth() > 0f) {
            exportBorder(leftPen, x, y - topPen.getLineWidth() / 2, 0, height + (topPen.getLineWidth() + bottomPen.getLineWidth()) / 2);
        }
    }

    private void exportBottomPen(JRPen leftPen, JRPen bottomPen, JRPen rightPen, int x, int y, int width, int height) throws IOException {
        if (bottomPen.getLineWidth() > 0f) {
            exportBorder(bottomPen, x - leftPen.getLineWidth() / 2, y + height, width + (leftPen.getLineWidth() + rightPen.getLineWidth()) / 2, 0);
        }
    }

    private void exportRightPen(JRPen topPen, JRPen bottomPen, JRPen rightPen, int x, int y, int width, int height) throws IOException {
        if (rightPen.getLineWidth() > 0f) {
            exportBorder(rightPen, x + width, y - topPen.getLineWidth() / 2, 0, height + (topPen.getLineWidth() + bottomPen.getLineWidth()) / 2);
        }
    }

    private static class InternalImageProcessorResult {

        protected final byte[] imageData;
        protected final Dimension2D dimension;
        protected final ImageTypeEnum imageType;

        protected InternalImageProcessorResult(byte[] imageData, Dimension2D dimension, ImageTypeEnum imageType) {
            this.imageData = imageData;
            this.dimension = dimension;
            this.imageType = imageType;
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

            DataRenderable imageRenderer = getRendererUtil().getImageDataRenderable(
                    imageRenderersCache, renderer,
                    new Dimension(availableImageWidth, availableImageHeight),
                    ModeEnum.OPAQUE == imageElement.getModeValue() ? imageElement.getBackcolor() : null);

            byte[] imageData = imageRenderer.getData(jasperReportsContext);

            return new InternalImageProcessorResult(imageData, dimension, JRTypeSniffer.getImageTypeValue(imageData));
        }

    }

}
