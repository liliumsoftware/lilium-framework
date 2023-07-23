package net.sf.dynamicreports.report.defaults;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import net.sf.dynamicreports.report.defaults.xml.XmlDynamicReports;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class Defaults {
    private static final Log log = LogFactory.getLog(Defaults.class);

    private static final Default defaults;

    static {
        defaults = DefaultBinder.bind(load());
    }

    private static XmlDynamicReports load() {
        String resource = "dynamicreports-defaults.xml";
        InputStream is = null;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            is = classLoader.getResourceAsStream(resource);
        }
        if (is == null) {
            classLoader = Defaults.class.getClassLoader();
            if (classLoader != null) {
                is = classLoader.getResourceAsStream(resource);
            }
            if (is == null) {
                is = Defaults.class.getResourceAsStream("/" + resource);
            }
        }
        if (is == null) {
            return null;
        }

        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(XmlDynamicReports.class).createUnmarshaller();
            JAXBElement<XmlDynamicReports> root = unmarshaller.unmarshal(new StreamSource(is), XmlDynamicReports.class);
            return root.getValue();
        } catch (JAXBException e) {
            log.error("Could not load dynamic reports defaults", e);
            return null;
        }
    }

    public static Default getDefaults() {
        return defaults;
    }

}
