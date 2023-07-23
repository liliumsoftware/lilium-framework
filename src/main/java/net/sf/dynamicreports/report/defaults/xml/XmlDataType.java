package net.sf.dynamicreports.report.defaults.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataType")
public class XmlDataType {

    @XmlAttribute(name = "pattern")
    protected String pattern;
    @XmlAttribute(name = "horizontalAlignment")
    protected XmlHorizontalAlignment horizontalAlignment;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String value) {
        this.pattern = value;
    }

    public XmlHorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(XmlHorizontalAlignment value) {
        this.horizontalAlignment = value;
    }

}
