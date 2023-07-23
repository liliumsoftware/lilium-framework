package ir.baho.framework.repository.impl.jasper;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.PrintPageFormat;
import net.sf.jasperreports.engine.export.oasis.ContentBuilder;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.oasis.OasisZip;
import net.sf.jasperreports.engine.export.oasis.OdtZip;
import net.sf.jasperreports.engine.export.oasis.StyleBuilder;
import net.sf.jasperreports.engine.export.oasis.WriterHelper;
import net.sf.jasperreports.engine.export.zip.ExportZipEntry;
import net.sf.jasperreports.engine.export.zip.FileBufferedZipEntry;
import net.sf.jasperreports.export.ExportInterruptedException;
import net.sf.jasperreports.export.ExporterInputItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@RequiredArgsConstructor
public class JasperOdtExporter extends JROdtExporter {

    private final boolean rtl;

    @Override
    protected void exportReportToOasisZip(OutputStream os) throws JRException, IOException {
        OasisZip oasisZip = new OdtZip();

        ExportZipEntry tempBodyEntry = new FileBufferedZipEntry(null);
        ExportZipEntry tempStyleEntry = new FileBufferedZipEntry(null);

        tempBodyWriter = new WriterHelper(jasperReportsContext, tempBodyEntry.getWriter());
        tempStyleWriter = new WriterHelper(jasperReportsContext, tempStyleEntry.getWriter());

        documentBuilder = new OdtDocumentBuilder(oasisZip);

        styleCache = new JasperStyleCache(jasperReportsContext, tempStyleWriter, getExporterKey(), rtl);

        WriterHelper stylesWriter = new WriterHelper(jasperReportsContext, oasisZip.getStylesEntry().getWriter());

        List<ExporterInputItem> items = exporterInput.getItems();

        StyleBuilder styleBuilder = new StyleBuilder(stylesWriter);

        styleBuilder.buildBeforeAutomaticStyles(jasperPrint);

        pageFormatIndex = -1;

        for (reportIndex = 0; reportIndex < items.size(); reportIndex++) {
            ExporterInputItem item = items.get(reportIndex);
            rowStyles.clear();
            columnStyles.clear();

            setCurrentExporterInputItem(item);

            List<JRPrintPage> pages = jasperPrint.getPages();
            if (pages != null && pages.size() > 0) {
                PageRange pageRange = getPageRange();
                int startPageIndex = (pageRange == null || pageRange.getStartPageIndex() == null) ? 0 : pageRange.getStartPageIndex();
                int endPageIndex = (pageRange == null || pageRange.getEndPageIndex() == null) ? (pages.size() - 1) : pageRange.getEndPageIndex();

                PrintPageFormat oldPageFormat = null;
                JRPrintPage page;
                for (pageIndex = startPageIndex; pageIndex <= endPageIndex; pageIndex++) {
                    if (Thread.interrupted()) {
                        throw new ExportInterruptedException();
                    }

                    PrintPageFormat pageFormat = jasperPrint.getPageFormat(pageIndex);

                    if (oldPageFormat != pageFormat) {
                        styleBuilder.buildPageLayout(++pageFormatIndex, pageFormat);
                        oldPageFormat = pageFormat;
                    }

                    page = pages.get(pageIndex);

                    exportPage(page);
                }
            }
        }

        styleBuilder.buildMasterPages(pageFormatIndex);

        stylesWriter.flush();
        tempBodyWriter.flush();
        tempStyleWriter.flush();

        stylesWriter.close();
        tempBodyWriter.close();
        tempStyleWriter.close();


        ContentBuilder contentBuilder = new ContentBuilder(oasisZip.getContentEntry(),
                tempStyleEntry, tempBodyEntry, styleCache.getFontFaces(), OasisZip.MIME_TYPE_ODT);
        contentBuilder.build();

        tempStyleEntry.dispose();
        tempBodyEntry.dispose();

        oasisZip.zipEntries(os);

        oasisZip.dispose();
    }

}
