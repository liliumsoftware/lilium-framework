package net.sf.dynamicreports.report.defaults.xml;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    private final static QName _DynamicReports_QNAME = new QName("", "DynamicReports");

    public ObjectFactory() {
    }

    public XmlDynamicReports createXmlDynamicReports() {
        return new XmlDynamicReports();
    }

    public XmlFont createXmlFont() {
        return new XmlFont();
    }

    public XmlDataType createXmlDataType() {
        return new XmlDataType();
    }

    @XmlElementDecl(namespace = "", name = "DynamicReports")
    public JAXBElement<XmlDynamicReports> createDynamicReports(XmlDynamicReports value) {
        return new JAXBElement<>(_DynamicReports_QNAME, XmlDynamicReports.class, null, value);
    }

}
