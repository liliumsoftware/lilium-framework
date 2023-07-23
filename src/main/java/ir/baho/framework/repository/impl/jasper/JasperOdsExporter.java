package ir.baho.framework.repository.impl.jasper;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.OdsZip;
import net.sf.jasperreports.engine.export.oasis.StyleBuilder;
import net.sf.jasperreports.engine.export.oasis.WriterHelper;
import net.sf.jasperreports.engine.export.zip.FileBufferedZipEntry;

import java.io.IOException;
import java.io.OutputStream;

@RequiredArgsConstructor
public class JasperOdsExporter extends JROdsExporter {

    private final boolean rtl;

    @Override
    protected void openWorkbook(OutputStream os) throws IOException {
        oasisZip = new OdsZip();

        tempBodyEntry = new FileBufferedZipEntry(null);
        tempStyleEntry = new FileBufferedZipEntry(null);

        tempBodyWriter = new WriterHelper(jasperReportsContext, tempBodyEntry.getWriter());
        tempStyleWriter = new WriterHelper(jasperReportsContext, tempStyleEntry.getWriter());

        rowStyles.clear();
        columnStyles.clear();
        documentBuilder = new OdsDocumentBuilder(oasisZip);

        styleCache = new JasperStyleCache(jasperReportsContext, tempStyleWriter, getExporterKey(), rtl);

        stylesWriter = new WriterHelper(jasperReportsContext, oasisZip.getStylesEntry().getWriter());

        styleBuilder = new StyleBuilder(stylesWriter);
        styleBuilder.buildBeforeAutomaticStyles(jasperPrint);

        namedExpressions = new StringBuilder("<table:named-expressions>\n");

        pageFormatIndex = -1;

        maxColumnIndex = 1023;
    }

}
