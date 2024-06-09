package ir.baho.framework.repository.impl.jasper;

import net.sf.jasperreports.engine.export.CutsInfo;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.export.ooxml.XlsxCellHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxDrawingHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxDrawingRelsHelper;
import net.sf.jasperreports.engine.export.ooxml.XlsxSheetRelsHelper;
import net.sf.jasperreports.engine.export.zip.ExportZipEntry;
import net.sf.jasperreports.export.XlsxReportConfiguration;

import java.io.Writer;

public class JasperXlsxExporter extends JRXlsxExporter {

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

}
