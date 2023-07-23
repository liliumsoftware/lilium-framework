package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.export.CutsInfo;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.export.ooxml.XlsxCellHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxDrawingHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxDrawingRelsHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxRunHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxSheetRelsHelper;
import net.sf.jasperreports.engine.export.zip.ExportZipEntry;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.export.XlsxReportConfiguration;

import java.io.Writer;
import java.text.AttributedCharacterIterator;
import java.util.Locale;

public class JasperXlsxExporter extends JRXlsxExporter {

    private XlsxRunHelper runHelper;

    @Override
    protected void createSheet(CutsInfo xCuts, SheetInfo sheetInfo) {
        startPage = true;
        currentSheetPageScale = sheetInfo.sheetPageScale;
        currentSheetFirstPageNumber = sheetInfo.sheetFirstPageNumber;
        currentSheetName = sheetInfo.sheetName;
        firstSheetName = firstSheetName == null ? currentSheetName : firstSheetName;
        wbHelper.exportSheet(sheetIndex + 1, currentSheetName, sheetMapping);
        ctHelper.exportSheet(sheetIndex + 1);
        relsHelper.exportSheet(sheetIndex + 1);
        XlsxReportConfiguration configuration = getCurrentItemConfiguration();
        ExportZipEntry sheetRelsEntry = xlsxZip.addSheetRels(sheetIndex + 1);
        Writer sheetRelsWriter = sheetRelsEntry.getWriter();
        sheetRelsHelper = new XlsxSheetRelsHelper(jasperReportsContext, sheetRelsWriter);

        ExportZipEntry sheetEntry = xlsxZip.addSheet(sheetIndex + 1);
        Writer sheetWriter = sheetEntry.getWriter();
        sheetHelper = new JasperXlsxSheetHelper(
                jasperReportsContext,
                sheetWriter,
                sheetRelsHelper,
                configuration
        );

        ExportZipEntry drawingRelsEntry = xlsxZip.addDrawingRels(sheetIndex + 1);
        Writer drawingRelsWriter = drawingRelsEntry.getWriter();
        drawingRelsHelper = new XlsxDrawingRelsHelper(jasperReportsContext, drawingRelsWriter);

        ExportZipEntry drawingEntry = xlsxZip.addDrawing(sheetIndex + 1);
        Writer drawingWriter = drawingEntry.getWriter();
        drawingHelper = new XlsxDrawingHelper(jasperReportsContext, drawingWriter, drawingRelsHelper);

        cellHelper = new XlsxCellHelper(jasperReportsContext, sheetWriter, styleHelper);
        runHelper = new XlsxRunHelper(jasperReportsContext, sheetWriter, getExporterKey());

        boolean showGridlines = true;
        if (sheetInfo.sheetShowGridlines == null) {
            Boolean documentShowGridlines = configuration.isShowGridLines();
            if (documentShowGridlines != null) {
                showGridlines = documentShowGridlines;
            }
        } else {
            showGridlines = sheetInfo.sheetShowGridlines;
        }
        sheetHelper.exportHeader(
                showGridlines,
                (sheetInfo.sheetPageScale == null ? 0 : sheetInfo.sheetPageScale),
                sheetInfo.rowFreezeIndex,
                sheetInfo.columnFreezeIndex,
                maxColumnIndex,
                jasperPrint,
                sheetInfo.tabColor);
        sheetRelsHelper.exportHeader(sheetIndex + 1);
        drawingHelper.exportHeader();
        drawingRelsHelper.exportHeader();
    }

    @Override
    protected void exportStyledText(JRStyle style, JRStyledText styledText, Locale locale, boolean isStyledText) {
        String text = styledText.getText();
        int runLimit = 0;
        AttributedCharacterIterator iterator = styledText.getAttributedString().getIterator();

        while (runLimit < styledText.length() && (runLimit = iterator.getRunLimit()) <= styledText.length()) {
            runHelper.export(
                    style, iterator.getAttributes(),
                    text.substring(iterator.getIndex(), runLimit),
                    locale,
                    invalidCharReplacement,
                    isStyledText
            );
            iterator.setIndex(runLimit);
        }
    }

}
