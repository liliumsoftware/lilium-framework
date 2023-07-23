package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JRDefaultStyleProvider;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.base.JRBasePrintText;
import net.sf.jasperreports.engine.export.ooxml.BaseFontHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxRunHelper;
import net.sf.jasperreports.engine.type.ColorEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRColorUtil;
import net.sf.jasperreports.engine.util.JRStringUtil;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class JasperDocxRunHelper extends DocxRunHelper {

    private final BaseFontHelper docxFontHelper;
    private final boolean rtl;

    public JasperDocxRunHelper(JasperReportsContext jasperReportsContext, Writer writer, BaseFontHelper docxFontHelper, boolean rtl) {
        super(jasperReportsContext, writer, docxFontHelper);
        this.docxFontHelper = docxFontHelper;
        this.rtl = rtl;
    }

    @Override
    public void export(JRStyle style, Map<AttributedCharacterIterator.Attribute, Object> attributes, String text,
                       Locale locale, boolean hiddenText, String invalidCharReplacement, Color backcolor, boolean isNewLineAsParagraph) {
        if (text != null) {
            write("      <w:r>\n");
            boolean highlightText = backcolor == null || !backcolor.equals(attributes.get(TextAttribute.BACKGROUND));
            exportProps(getAttributes(style), attributes, locale, hiddenText, highlightText);

            StringTokenizer tkzer = new StringTokenizer(text, "\n", true);
            while (tkzer.hasMoreTokens()) {
                String token = tkzer.nextToken();
                if ("\n".equals(token)) {
                    if (isNewLineAsParagraph) {
                        write("<w:t xml:space=\"preserve\"><w:p/></w:t>\n");
                    } else {
                        write("<w:br/>");
                    }
                } else {
                    write("<w:t xml:space=\"preserve\">");
                    write(JRStringUtil.xmlEncode(token, invalidCharReplacement));
                    write("</w:t>\n");
                }
            }
            write("      </w:r>\n");
        }
    }

    @Override
    public void exportProps(JRDefaultStyleProvider defaultStyleProvider, JRStyle style, Locale locale) {
        JRStyle baseStyle = defaultStyleProvider.getStyleResolver().getBaseStyle(style);
        exportProps(getAttributes(baseStyle), getAttributes(style), locale, false, false);
    }

    @Override
    public void exportProps(Map<AttributedCharacterIterator.Attribute, Object> parentAttrs,
                            Map<AttributedCharacterIterator.Attribute, Object> attrs, Locale locale,
                            boolean hiddenText, boolean highlightText) {
        write("       <w:rPr>\n");

        Object valueWeight = attrs.get(TextAttribute.WEIGHT);
        Object oldValueWeight = parentAttrs.get(TextAttribute.WEIGHT);

        Object valuePosture = attrs.get(TextAttribute.POSTURE);
        Object oldValuePosture = parentAttrs.get(TextAttribute.POSTURE);

        String fontFamily = docxFontHelper.resolveFontFamily(attrs, locale);
        write("        <w:rFonts w:ascii=\"" + fontFamily + "\" w:hAnsi=\"" + fontFamily + "\" w:eastAsia=\"" + fontFamily + "\" w:cs=\"" + fontFamily + "\" />\n");
        write("        <w:textDirection w:val=\"btLr\"/>\n");
        if (rtl) {
            write("        <w:rtl/>\n");
        }

        Object value = attrs.get(TextAttribute.FOREGROUND);
        Object oldValue = parentAttrs.get(TextAttribute.FOREGROUND);

        if (value != null && !value.equals(oldValue)) {
            write("        <w:color w:val=\"" + JRColorUtil.getColorHexa((Color) value) + "\" />\n");
        }

        if (highlightText) {
            value = attrs.get(TextAttribute.BACKGROUND);

            if (value != null && ColorEnum.getByColor((Color) value) != null) {
                write("        <w:highlight w:val=\"" + ColorEnum.getByColor((Color) value).getName() + "\" />\n");
            }
        }

        value = attrs.get(TextAttribute.SIZE);
        oldValue = parentAttrs.get(TextAttribute.SIZE);

        if (value != null && !value.equals(oldValue)) {
            float fontSize = (Float) value;
            fontSize = fontSize == 0 ? 0.5f : fontSize;// only the special EMPTY_CELL_STYLE would have font size zero
            write("        <w:sz w:val=\"" + (int) (2 * fontSize) + "\" />\n");
        }

        if (valueWeight != null && !valueWeight.equals(oldValueWeight)) {
            write("        <w:b w:val=\"" + valueWeight.equals(TextAttribute.WEIGHT_BOLD) + "\"/>\n");
        }

        if (valuePosture != null && !valuePosture.equals(oldValuePosture)) {
            write("        <w:i w:val=\"" + valuePosture.equals(TextAttribute.POSTURE_OBLIQUE) + "\"/>\n");
        }


        value = attrs.get(TextAttribute.UNDERLINE);
        oldValue = parentAttrs.get(TextAttribute.UNDERLINE);

        if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
            write("        <w:u w:val=\"" + (value == null ? "none" : "single") + "\"/>\n");
        }

        value = attrs.get(TextAttribute.STRIKETHROUGH);
        oldValue = parentAttrs.get(TextAttribute.STRIKETHROUGH);

        if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
            write("        <w:strike w:val=\"" + (value != null) + "\"/>\n");
        }

        value = attrs.get(TextAttribute.SUPERSCRIPT);

        if (TextAttribute.SUPERSCRIPT_SUPER.equals(value)) {
            write("        <w:vertAlign w:val=\"superscript\" />\n");
        } else if (TextAttribute.SUPERSCRIPT_SUB.equals(value)) {
            write("        <w:vertAlign w:val=\"subscript\" />\n");
        }

        if (hiddenText) {
            write("        <w:vanish/>\n");
        }

        write("       </w:rPr>\n");
    }

    private Map<AttributedCharacterIterator.Attribute, Object> getAttributes(JRStyle style) {
        Map<AttributedCharacterIterator.Attribute, Object> styledTextAttributes = new HashMap<>();

        if (style != null) {
            JRPrintText text = new JRBasePrintText(null);
            text.setStyle(style);

            fontUtil.getAttributesWithoutAwtFont(styledTextAttributes, text);
            styledTextAttributes.put(TextAttribute.FOREGROUND, text.getForecolor());
            if (text.getModeValue() == ModeEnum.OPAQUE) {
                styledTextAttributes.put(TextAttribute.BACKGROUND, text.getBackcolor());
            }
        }

        return styledTextAttributes;
    }

}
