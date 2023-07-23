package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JRPrintElementIndex;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.PrintPageFormat;
import net.sf.jasperreports.engine.export.CutsInfo;
import net.sf.jasperreports.engine.export.ooxml.DocxTableHelper;

import java.io.Writer;

public class JasperDocxTableHelper extends DocxTableHelper {

    protected JasperDocxTableHelper(JasperReportsContext jasperReportsContext, Writer writer, CutsInfo xCuts, boolean pageBreak, PrintPageFormat pageFormat, JRPrintElementIndex frameIndex) {
        super(jasperReportsContext, writer, xCuts, pageBreak, pageFormat, frameIndex);
    }

}
