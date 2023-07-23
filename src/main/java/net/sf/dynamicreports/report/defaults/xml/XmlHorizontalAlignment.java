package net.sf.dynamicreports.report.defaults.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "HorizontalAlignment")
@XmlEnum
public enum XmlHorizontalAlignment {

    LEFT,
    CENTER,
    RIGHT,
    JUSTIFIED;

    public static XmlHorizontalAlignment fromValue(String v) {
        return valueOf(v);
    }

    public String value() {
        return name();
    }

}
