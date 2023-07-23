package ir.baho.framework.repository.impl.jasper;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGenericPrintElement;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintElementIndex;
import net.sf.jasperreports.engine.JRPrintEllipse;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintImage;
import net.sf.jasperreports.engine.JRPrintLine;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintRectangle;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.PrintPageFormat;
import net.sf.jasperreports.engine.export.CutsInfo;
import net.sf.jasperreports.engine.export.ElementGridCell;
import net.sf.jasperreports.engine.export.Grid;
import net.sf.jasperreports.engine.export.GridRow;
import net.sf.jasperreports.engine.export.JRExporterGridCell;
import net.sf.jasperreports.engine.export.JRGridLayout;
import net.sf.jasperreports.engine.export.OccupiedGridCell;
import net.sf.jasperreports.engine.export.ooxml.DocxDocumentHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxFontHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxFontTableHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxFontTableRelsHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxRelsHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxSettingsHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxStyleHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxTableHelper;
import net.sf.jasperreports.engine.export.ooxml.DocxZip;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.OoxmlEncryptUtil;
import net.sf.jasperreports.engine.export.ooxml.PropsAppHelper;
import net.sf.jasperreports.engine.export.ooxml.PropsCoreHelper;
import net.sf.jasperreports.export.DocxExporterConfiguration;
import net.sf.jasperreports.export.ExportInterruptedException;
import net.sf.jasperreports.export.ExporterInputItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@RequiredArgsConstructor
public class JasperDocxExporter extends JRDocxExporter {

    private final boolean rtl;

    boolean emptyPageState;

    @Override
    protected void exportReportToStream(OutputStream os) throws JRException, IOException {
        docxZip = new DocxZip();

        docWriter = docxZip.getDocumentEntry().getWriter();

        docHelper = new DocxDocumentHelper(jasperReportsContext, docWriter);
        docHelper.exportHeader();

        relsHelper = new DocxRelsHelper(jasperReportsContext, docxZip.getRelsEntry().getWriter());
        relsHelper.exportHeader();

        appHelper = new PropsAppHelper(jasperReportsContext, docxZip.getAppEntry().getWriter());
        coreHelper = new PropsCoreHelper(jasperReportsContext, docxZip.getCoreEntry().getWriter());

        appHelper.exportHeader();

        DocxExporterConfiguration configuration = getCurrentConfiguration();

        String application = configuration.getMetadataApplication();
        if (application == null) {
            application = "JasperReports Library version " + ClassLoader.getSystemClassLoader().getDefinedPackage("net.sf.jasperreports.engine").getImplementationVersion();
        }
        appHelper.exportProperty(PropsAppHelper.PROPERTY_APPLICATION, application);

        coreHelper.exportHeader();

        String title = configuration.getMetadataTitle();
        if (title != null) {
            coreHelper.exportProperty(PropsCoreHelper.PROPERTY_TITLE, title);
        }
        String subject = configuration.getMetadataSubject();
        if (subject != null) {
            coreHelper.exportProperty(PropsCoreHelper.PROPERTY_SUBJECT, subject);
        }
        String author = configuration.getMetadataAuthor();
        if (author != null) {
            coreHelper.exportProperty(PropsCoreHelper.PROPERTY_CREATOR, author);
        }
        String keywords = configuration.getMetadataKeywords();
        if (keywords != null) {
            coreHelper.exportProperty(PropsCoreHelper.PROPERTY_KEYWORDS, keywords);
        }

        List<ExporterInputItem> items = exporterInput.getItems();

        boolean isEmbedFonts = Boolean.TRUE.equals(configuration.isEmbedFonts());

        docxFontHelper = new DocxFontHelper(jasperReportsContext, docxZip, isEmbedFonts);

        DocxStyleHelper styleHelper = new DocxStyleHelper(this, docxZip.getStylesEntry().getWriter(), docxFontHelper);
        styleHelper.export(exporterInput);
        styleHelper.close();

        DocxSettingsHelper settingsHelper = new DocxSettingsHelper(jasperReportsContext, docxZip.getSettingsEntry().getWriter());
        settingsHelper.export(jasperPrint, isEmbedFonts);
        settingsHelper.close();

        docxFontTableHelper = new DocxFontTableHelper(jasperReportsContext, docxZip.getFontTableEntry().getWriter());
        docxFontTableHelper.exportHeader();

        docxFontTableRelsHelper = new DocxFontTableRelsHelper(jasperReportsContext, docxZip.getFontTableRelsEntry().getWriter());
        docxFontTableRelsHelper.exportHeader();


        runHelper = new JasperDocxRunHelper(jasperReportsContext, docWriter, docxFontHelper, rtl);

        pageFormat = null;
        PrintPageFormat oldPageFormat = null;

        for (reportIndex = 0; reportIndex < items.size(); reportIndex++) {
            ExporterInputItem item = items.get(reportIndex);

            setCurrentExporterInputItem(item);

            bookmarkIndex = 0;
            emptyPageState = false;

            List<JRPrintPage> pages = jasperPrint.getPages();
            if (pages != null && pages.size() > 0) {
                PageRange pageRange = getPageRange();
                startPageIndex = (pageRange == null || pageRange.getStartPageIndex() == null) ? 0 : pageRange.getStartPageIndex();
                endPageIndex = (pageRange == null || pageRange.getEndPageIndex() == null) ? (pages.size() - 1) : pageRange.getEndPageIndex();

                JRPrintPage page;
                for (pageIndex = startPageIndex; pageIndex <= endPageIndex; pageIndex++) {
                    if (Thread.interrupted()) {
                        throw new ExportInterruptedException();
                    }

                    page = pages.get(pageIndex);

                    pageFormat = jasperPrint.getPageFormat(pageIndex);

                    if (oldPageFormat != null && oldPageFormat != pageFormat) {
                        docHelper.exportSection(oldPageFormat, pageGridLayout, false);
                    }

                    exportPage(page);

                    oldPageFormat = pageFormat;
                }
            }
        }

        if (oldPageFormat != null) {
            docHelper.exportSection(oldPageFormat, pageGridLayout, true);
        }

        docHelper.exportFooter();
        docHelper.close();

        relsHelper.exportFooter();
        relsHelper.close();

        appHelper.exportFooter();
        appHelper.close();

        coreHelper.exportFooter();
        coreHelper.close();

        docxFontHelper.exportFonts();

        docxFontTableHelper.exportFooter();
        docxFontTableHelper.close();

        docxFontTableRelsHelper.exportFooter();
        docxFontTableRelsHelper.close();

        String password = getCurrentConfiguration().getEncryptionPassword();
        if (password == null || password.trim().length() == 0) {
            docxZip.zipEntries(os);
        } else {
            OoxmlEncryptUtil.zipEntries(docxZip, os, password);
        }

        docxZip.dispose();
    }

    @Override
    protected void exportGrid(JRGridLayout gridLayout, JRPrintElementIndex frameIndex) throws JRException {

        CutsInfo xCuts = gridLayout.getXCuts();
        Grid grid = gridLayout.getGrid();
        DocxTableHelper tableHelper;

        int rowCount = grid.getRowCount();
        if (rowCount > 0 && grid.getColumnCount() > 63) {
            throw new JRException(EXCEPTION_MESSAGE_KEY_COLUMN_COUNT_OUT_OF_RANGE, grid.getColumnCount());
        }

        if (rowCount == 0 && (pageIndex < endPageIndex || !emptyPageState)) {
            tableHelper = new JasperDocxTableHelper(jasperReportsContext, docWriter, xCuts, false, pageFormat, frameIndex);
            int maxReportIndex = exporterInput.getItems().size() - 1;

            boolean twice =
                    (pageIndex > startPageIndex && pageIndex < endPageIndex && !emptyPageState)
                            || (reportIndex < maxReportIndex && pageIndex == endPageIndex);
            tableHelper.getParagraphHelper().exportEmptyPage(pageAnchor, bookmarkIndex, twice);
            bookmarkIndex++;
            emptyPageState = true;
            return;
        }

        tableHelper = new JasperDocxTableHelper(jasperReportsContext, docWriter, xCuts,
                frameIndex == null && (reportIndex != 0 || pageIndex != startPageIndex), pageFormat, frameIndex);

        tableHelper.exportHeader();

        boolean isFlexibleRowHeight = getCurrentItemConfiguration().isFlexibleRowHeight();

        for (int row = 0; row < rowCount; row++) {
            int emptyCellColSpan = 0;

            boolean allowRowResize = false;
            int maxTopPadding = 0;
            int maxBottomPadding = 0;
            GridRow gridRow = grid.getRow(row);
            int rowSize = gridRow.size();
            for (int col = 0; col < rowSize; col++) {
                JRExporterGridCell gridCell = gridRow.get(col);
                JRLineBox box = gridCell.getBox();
                if (box != null) {
                    int topPadding = box.getTopPadding() + Math.round(box.getTopPen().getLineWidth());
                    if (maxTopPadding < topPadding) {
                        maxTopPadding = topPadding;
                    }

                    Integer bottomPadding = box.getBottomPadding();
                    if (bottomPadding != null && maxBottomPadding < bottomPadding) {
                        maxBottomPadding = bottomPadding;
                    }
                }

                if (isFlexibleRowHeight && !allowRowResize) {
                    JRPrintElement cellElement = gridCell.getElement();
                    if (gridCell.getType() == JRExporterGridCell.TYPE_OCCUPIED_CELL) {
                        cellElement = ((OccupiedGridCell) gridCell).getOccupier().getElement();
                    }
                    allowRowResize = cellElement instanceof JRPrintText || cellElement instanceof JRPrintFrame;
                }
            }
            tableHelper.setRowMaxTopPadding(maxTopPadding);

            int rowHeight = gridLayout.getRowHeight(row) - maxBottomPadding;
            if (row == 0 && frameIndex == null) {
                rowHeight -= Math.min(rowHeight, pageFormat.getTopMargin());
            }

            tableHelper.exportRowHeader(
                    rowHeight,
                    allowRowResize
            );

            for (int col = 0; col < rowSize; col++) {
                JRExporterGridCell gridCell = gridRow.get(col);
                if (gridCell.getType() == JRExporterGridCell.TYPE_OCCUPIED_CELL) {
                    if (emptyCellColSpan > 0) {
                        emptyCellColSpan = 0;
                    }

                    OccupiedGridCell occupiedGridCell = (OccupiedGridCell) gridCell;
                    ElementGridCell elementGridCell = (ElementGridCell) occupiedGridCell.getOccupier();
                    tableHelper.exportOccupiedCells(elementGridCell, startPage, bookmarkIndex, pageAnchor);
                    if (startPage) {
                        bookmarkIndex++;
                    }
                    col += elementGridCell.getColSpan() - 1;
                } else if (gridCell.getType() == JRExporterGridCell.TYPE_ELEMENT_CELL) {
                    if (emptyCellColSpan > 0) {
                        emptyCellColSpan = 0;
                    }

                    JRPrintElement element = gridCell.getElement();

                    if (element instanceof JRPrintLine) {
                        exportLine(tableHelper, (JRPrintLine) element, gridCell);
                    } else if (element instanceof JRPrintRectangle) {
                        exportRectangle(tableHelper, (JRPrintRectangle) element, gridCell);
                    } else if (element instanceof JRPrintEllipse) {
                        exportEllipse(tableHelper, (JRPrintEllipse) element, gridCell);
                    } else if (element instanceof JRPrintImage) {
                        exportImage(tableHelper, (JRPrintImage) element, gridCell);
                    } else if (element instanceof JRPrintText) {
                        exportText(tableHelper, (JRPrintText) element, gridCell);
                    } else if (element instanceof JRPrintFrame) {
                        exportFrame(tableHelper, (JRPrintFrame) element, gridCell);
                    } else if (element instanceof JRGenericPrintElement) {
                        exportGenericElement(tableHelper, (JRGenericPrintElement) element, gridCell);
                    }

                    col += gridCell.getColSpan() - 1;
                } else {
                    emptyCellColSpan++;
                    tableHelper.exportEmptyCell(gridCell, 1, startPage, bookmarkIndex, pageAnchor);
                    if (startPage) {
                        bookmarkIndex++;
                    }
                }
                startPage = false;
            }

            tableHelper.exportRowFooter();
        }

        tableHelper.exportFooter();
        emptyPageState = false;
    }

}
