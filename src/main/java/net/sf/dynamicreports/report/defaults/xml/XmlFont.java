package net.sf.dynamicreports.report.defaults.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Font")
public class XmlFont {

    @XmlAttribute(name = "fontName")
    protected String fontName;
    @XmlAttribute(name = "fontSize")
    protected Integer fontSize;
    @XmlAttribute(name = "pdfFontName")
    protected String pdfFontName;
    @XmlAttribute(name = "pdfEncoding")
    protected String pdfEncoding;
    @XmlAttribute(name = "pdfEmbedded")
    protected Boolean pdfEmbedded;

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String value) {
        this.fontName = value;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer value) {
        this.fontSize = value;
    }

    public String getPdfFontName() {
        return pdfFontName;
    }

    public void setPdfFontName(String value) {
        this.pdfFontName = value;
    }

    public String getPdfEncoding() {
        return pdfEncoding;
    }

    public void setPdfEncoding(String value) {
        this.pdfEncoding = value;
    }

    public Boolean isPdfEmbedded() {
        return pdfEmbedded;
    }

    public void setPdfEmbedded(Boolean value) {
        this.pdfEmbedded = value;
    }

}
