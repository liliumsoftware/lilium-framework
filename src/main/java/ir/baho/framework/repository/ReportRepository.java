package ir.baho.framework.repository;

import ir.baho.framework.converter.EnumConverter;
import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.enumeration.EnumValue;
import ir.baho.framework.exception.MetadataFieldAccessException;
import ir.baho.framework.i18n.MessageResource;
import ir.baho.framework.metadata.ExportType;
import ir.baho.framework.metadata.ReportMetadata;
import ir.baho.framework.metadata.Sort;
import ir.baho.framework.metadata.StaticReportMetadata;
import ir.baho.framework.metadata.report.AreaChart;
import ir.baho.framework.metadata.report.BarChart;
import ir.baho.framework.metadata.report.Border;
import ir.baho.framework.metadata.report.Chart;
import ir.baho.framework.metadata.report.DateTimeFormatters;
import ir.baho.framework.metadata.report.Font;
import ir.baho.framework.metadata.report.LineChart;
import ir.baho.framework.metadata.report.PieChart;
import ir.baho.framework.metadata.report.Position;
import ir.baho.framework.metadata.report.ReportColumn;
import ir.baho.framework.metadata.report.ReportDesign;
import ir.baho.framework.metadata.report.ReportType;
import ir.baho.framework.metadata.report.Style;
import ir.baho.framework.metadata.report.Subtotal;
import ir.baho.framework.metadata.report.TextFormat;
import ir.baho.framework.repository.impl.jasper.JasperDocxExporter;
import ir.baho.framework.repository.impl.jasper.JasperOdsExporter;
import ir.baho.framework.repository.impl.jasper.JasperOdtExporter;
import ir.baho.framework.repository.impl.jasper.JasperPptxExporter;
import ir.baho.framework.repository.impl.jasper.JasperRtfExporter;
import ir.baho.framework.repository.impl.jasper.JasperXlsxExporter;
import ir.baho.framework.repository.impl.jasper.Styles;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.chart.AbstractBaseChartBuilder;
import net.sf.dynamicreports.report.builder.chart.AxisFormatBuilder;
import net.sf.dynamicreports.report.builder.chart.CategoryChartSerieBuilder;
import net.sf.dynamicreports.report.builder.column.BooleanColumnBuilder;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.column.ValueColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.PageNumberBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.builder.subtotal.SubtotalBuilder;
import net.sf.dynamicreports.report.constant.BooleanComponentType;
import net.sf.dynamicreports.report.constant.GroupHeaderLayout;
import net.sf.dynamicreports.report.constant.Orientation;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.WhenNoDataType;
import net.sf.dynamicreports.report.constant.WhenResourceMissingType;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintFrame;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.type.RunDirectionEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterConfiguration;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.awt.Color;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public interface ReportRepository<E, ID> extends BaseRepository<E, ID> {

    int VIRTUALIZER_MAX_SIZE = 300;
    String VIRTUALIZER_TEMP_DIR = System.getProperty("java.io.tmpdir");

    void export(JasperReportBuilder builder, ExportType type, OutputStream outputStream);

    JasperPrint report(StaticReportMetadata metadata, InputStream inputStream);

    void save(Path path, JasperReportBuilder reportBuilder);

    @SneakyThrows
    default void save(Path path, StaticReportMetadata metadata, InputStream inputStream) {
        JRSaver.saveObject(report(metadata, inputStream), path.toFile());
    }

    @SneakyThrows
    default void export(Path path, ExportType type, OutputStream outputStream) {
        export(JRLoader.loadJasperPrint(path.toFile(), new JRFileVirtualizer(VIRTUALIZER_MAX_SIZE, VIRTUALIZER_TEMP_DIR)), type, outputStream);
    }

    @SneakyThrows
    default void export(JasperPrint print, ExportType type, OutputStream outputStream) {
        boolean rtl = print.getLocaleCode().startsWith("fa") || print.getLocaleCode().startsWith("ar");
        if (rtl && (type == ExportType.pdf || type == ExportType.rtf || type == ExportType.docx || type == ExportType.pptx)) {
            mirrorLayout(print);
        }
        switch (type) {
            case pdf -> {
                JRPdfExporter pdfExporter = new JRPdfExporter();
                SimplePdfExporterConfiguration pdfExporterConfiguration = new SimplePdfExporterConfiguration();
                pdfExporterConfiguration.setCompressed(true);
                pdfExporter.setConfiguration(pdfExporterConfiguration);
                pdfExporter.setExporterInput(new SimpleExporterInput(print));
                pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                pdfExporter.exportReport();
            }
            case csv -> {
                JRCsvExporter csvExporter = new JRCsvExporter();
                csvExporter.setExporterInput(new SimpleExporterInput(print));
                csvExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
                csvExporter.exportReport();
            }
            case xlsx -> {
                JRXlsxExporter xlsxExporter = new JasperXlsxExporter();
                SimpleXlsxReportConfiguration xlsReportConfiguration = new SimpleXlsxReportConfiguration();
                xlsReportConfiguration.setOnePagePerSheet(false);
                xlsReportConfiguration.setRemoveEmptySpaceBetweenRows(true);
                xlsReportConfiguration.setRemoveEmptySpaceBetweenColumns(true);
                xlsReportConfiguration.setWhitePageBackground(false);
                xlsReportConfiguration.setDetectCellType(true);
                if (rtl) {
                    xlsReportConfiguration.setSheetDirection(RunDirectionEnum.RTL);
                }
                xlsxExporter.setConfiguration(xlsReportConfiguration);
                xlsxExporter.setExporterInput(new SimpleExporterInput(print));
                xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                xlsxExporter.exportReport();
            }
            case docx -> {
                JRDocxExporter docxExporter = new JasperDocxExporter(rtl);
                docxExporter.setExporterInput(new SimpleExporterInput(print));
                docxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                docxExporter.exportReport();
            }
            case pptx -> {
                JRPptxExporter pptxExporter = new JasperPptxExporter(rtl);
                pptxExporter.setExporterInput(new SimpleExporterInput(print));
                pptxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                pptxExporter.exportReport();
            }
            case odt -> {
                JROdtExporter odtExporter = new JasperOdtExporter(rtl);
                odtExporter.setExporterInput(new SimpleExporterInput(print));
                odtExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                odtExporter.exportReport();
            }
            case ods -> {
                JROdsExporter odsExporter = new JasperOdsExporter(rtl);
                odsExporter.setExporterInput(new SimpleExporterInput(print));
                odsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                odsExporter.exportReport();
            }
            case rtf -> {
                JRRtfExporter rtfExporter = new JasperRtfExporter();
                rtfExporter.setExporterInput(new SimpleExporterInput(print));
                rtfExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
                rtfExporter.exportReport();
            }
            case html -> {
                HtmlExporter htmlExporter = new HtmlExporter();
                StringBuilder header = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
                header.append("<html>\n");
                header.append("<head>\n");
                header.append("  <title>");
                header.append(print.getName());
                header.append("</title>\n");
                header.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
                header.append("  <style type=\"text/css\">\n");
                header.append("    a {text-decoration: none}\n");
                for (Font font : Font.values()) {
                    header.append("    @font-face {\n");
                    header.append("      font-family: ").append(font.name()).append(";\n");
                    header.append("      src: url(data:font/ttf;charset=utf-8;base64,").append(font.getBase64()).append(") format('truetype');\n");
                    header.append("    }\n");
                }
                header.append("  </style>\n");
                header.append("</head>\n");
                header.append("<body");
                if (rtl) {
                    header.append(" dir=\"rtl\"");
                }
                header.append(" text=\"#000000\" link=\"#000000\" alink=\"#000000\" vlink=\"#000000\">\n");
                header.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n");
                header.append("<tr><td width=\"50%\">&nbsp;</td><td align=\"center\">\n");
                SimpleHtmlExporterConfiguration htmlExporterConfiguration = new SimpleHtmlExporterConfiguration();
                htmlExporterConfiguration.setHtmlHeader(header.toString());
                htmlExporter.setConfiguration(htmlExporterConfiguration);
                htmlExporter.setExporterInput(new SimpleExporterInput(print));
                SimpleHtmlExporterOutput htmlExporterOutput = new SimpleHtmlExporterOutput(outputStream);
                htmlExporter.setExporterOutput(htmlExporterOutput);
                htmlExporter.exportReport();
            }
        }
    }

    default Class<?> getType(Class<?> type) {
        if (Temporal.class.isAssignableFrom(type)) {
            return Timestamp.class;
        } else if (TemporalAmount.class.isAssignableFrom(type)) {
            return String.class;
        } else if (Enum.class.isAssignableFrom(type)) {
            return String.class;
        } else if (byte.class == type) {
            return Byte.class;
        } else if (short.class == type) {
            return Short.class;
        } else if (int.class == type) {
            return Integer.class;
        } else if (long.class == type) {
            return Long.class;
        } else if (float.class == type) {
            return Float.class;
        } else if (double.class == type) {
            return Double.class;
        } else if (char.class == type) {
            return Character.class;
        }
        return type;
    }

    default JasperReportBuilder getDesign(ReportMetadata metadata, ReportDesign design, MessageResource messageResource,
                                          List<StringConverter<? extends Comparable<?>>> converters,
                                          Map<String, Class<?>> fields, Class<?> domainClass, boolean names) {
        JasperReportBuilder builder = DynamicReports.report()
                .setSummaryWithPageHeaderAndFooter(true)
                .setWhenNoDataType(WhenNoDataType.BLANK_PAGE)
                .setWhenResourceMissingType(WhenResourceMissingType.EMPTY);
        boolean rtl = metadata.isRtl();

        if (design.getTitle() != null) {
            TextFieldBuilder<String> title = DynamicReports.cmp.text(design.getTitle())
                    .setValueFormatter(StringConverter.stringConverter());
            if (design.getTitleStyle() != null) {
                title.setStyle(getStyle(design.getTitleStyle()));
            } else {
                title.setStyle(getStyle(Styles.title()));
            }
            builder.title(title);
        }
        if (design.getSubtitle() != null) {
            TextFieldBuilder<String> subtitle = DynamicReports.cmp.text(design.getSubtitle())
                    .setValueFormatter(StringConverter.stringConverter());
            if (design.getSubtitleStyle() != null) {
                subtitle.setStyle(getStyle(design.getSubtitleStyle()));
            } else {
                subtitle.setStyle(getStyle(Styles.subtitle(rtl)));
            }
            builder.addTitle(subtitle);
        }

        Columns columns = getColumns(metadata, design, messageResource, converters, fields, domainClass, names);
        if (design.getType() != ReportType.CHART_ONLY) {
            builder.addColumn(columns.getColumns().toArray(ColumnBuilder<?, ?>[]::new));

            if (!columns.getGroups().isEmpty()) {
                builder.groupBy(columns.getGroups().toArray(ColumnGroupBuilder[]::new));
            }

            if (!columns.getTitleSubtotals().isEmpty()) {
                builder.subtotalsAtTitle(columns.getTitleSubtotals().toArray(SubtotalBuilder<?, ?>[]::new));
            }
            if (!columns.getPageHeaderSubtotals().isEmpty()) {
                builder.subtotalsAtPageHeader(columns.getPageHeaderSubtotals().toArray(SubtotalBuilder<?, ?>[]::new));
            }
            if (!columns.getPageFooterSubtotals().isEmpty()) {
                builder.subtotalsAtPageFooter(columns.getPageFooterSubtotals().toArray(SubtotalBuilder<?, ?>[]::new));
            }
            if (!columns.getColumnHeaderSubtotals().isEmpty()) {
                builder.subtotalsAtColumnHeader(columns.getColumnHeaderSubtotals().toArray(SubtotalBuilder<?, ?>[]::new));
            }
            if (!columns.getColumnFooterSubtotals().isEmpty()) {
                builder.subtotalsAtColumnFooter(columns.getColumnFooterSubtotals().toArray(SubtotalBuilder<?, ?>[]::new));
            }
            if (!columns.getSummarySubtotals().isEmpty()) {
                builder.subtotalsAtSummary(columns.getSummarySubtotals().toArray(SubtotalBuilder<?, ?>[]::new));
            }
        }
        if (design.getType() == ReportType.CHART_ONLY || design.getType() == ReportType.CHART_TABLE) {
            builder.addTitle(getCharts(columns.getValueColumns(), design.getCharts()).toArray(ComponentBuilder[]::new));
        }

        if (design.getHeader() != null) {
            TextFieldBuilder<String> header = DynamicReports.cmp.text(design.getHeader())
                    .setValueFormatter(StringConverter.stringConverter());
            if (design.getHeaderStyle() != null) {
                header.setStyle(getStyle(design.getHeaderStyle()));
            } else {
                header.setStyle(getStyle(Styles.header(rtl)));
            }
            builder.addPageHeader(header);
        }

        TextFieldBuilder<String> username = DynamicReports.cmp
                .text(messageResource.getMessage("report.username") + " " + metadata.getUsername())
                .setValueFormatter(StringConverter.stringConverter());
        if (design.getUsernameStyle() != null) {
            username.setStyle(getStyle(design.getUsernameStyle()));
        } else {
            username.setStyle(getStyle(Styles.username(rtl)));
        }
        if (design.getUsernamePosition() == Position.TOP) {
            builder.addPageHeader(username);
        } else {
            builder.addPageFooter(username);
        }

        if (design.isHighlightAlternateRows()) {
            builder.highlightDetailOddRows();
        }
        if (design.getPaginate() != null) {
            builder.setIgnorePagination(!design.getPaginate());
        }

        TextFieldBuilder<?> dateTime = DynamicReports.cmp.text(metadata.getDateTimeFormatters().now());
        if (design.getDateTimeStyle() != null) {
            dateTime.setStyle(getStyle(design.getDateTimeStyle()));
        } else {
            dateTime.setStyle(getStyle(Styles.dateTime(rtl)));
        }
        if (design.getDateTimePosition() == Position.TOP) {
            builder.addPageHeader(dateTime);
        } else {
            builder.addPageFooter(dateTime);
        }

        if (design.getFooter() != null) {
            TextFieldBuilder<String> footer = DynamicReports.cmp.text(design.getFooter())
                    .setValueFormatter(StringConverter.stringConverter());
            if (design.getFooterStyle() != null) {
                footer.setStyle(getStyle(design.getFooterStyle()));
            } else {
                footer.setStyle(getStyle(Styles.footer(rtl)));
            }
            builder.addPageFooter(footer);
        }
        if (design.getType() == ReportType.TABLE_CHART) {
            builder.addSummary(getCharts(columns.getValueColumns(), design.getCharts()).toArray(ComponentBuilder[]::new));
        }
        if (design.getSummary() != null) {
            TextFieldBuilder<String> summary = DynamicReports.cmp.text(design.getSummary())
                    .setValueFormatter(StringConverter.stringConverter());
            if (design.getSummaryStyle() != null) {
                summary.setStyle(getStyle(design.getSummaryStyle()));
            } else {
                summary.setStyle(getStyle(Styles.summary(rtl)));
            }
            builder.summary(summary);
        }
        if (design.isPageNumber()) {
            PageNumberBuilder pageNumber = DynamicReports.cmp.pageNumber();
            if (design.getPageNumberStyle() != null) {
                pageNumber.setStyle(getStyle(design.getPageNumberStyle()));
            } else {
                pageNumber.setStyle(getStyle(Styles.pageNumber()));
            }
            builder.addPageFooter(pageNumber);
        }
        builder.setPageFormat(design.getWidth(), design.getHeight(), PageOrientation.PORTRAIT);
        builder.setPageMargin(DynamicReports.margin()
                .setTop(design.getMarginTop()).setLeft(design.getMarginLeft())
                .setRight(design.getMarginRight()).setBottom(design.getMarginBottom()));
        return builder;
    }

    @SuppressWarnings("unchecked")
    private Columns getColumns(ReportMetadata metadata, ReportDesign design, MessageResource messageResource,
                               List<StringConverter<? extends Comparable<?>>> converters,
                               Map<String, Class<?>> availableFields, Class<?> domainClass, boolean names) {
        Columns columns = new Columns();
        if (design.isRowNumber()) {
            TextColumnBuilder<Integer> rowNumber = DynamicReports.col
                    .reportRowNumberColumn(messageResource.getMessage("row.number"))
                    .setValueFormatter(StringConverter.stringConverter());
            if (design.getRowNumberHeaderStyle() != null) {
                rowNumber.setTitleStyle(getStyle(design.getRowNumberHeaderStyle()));
            } else {
                rowNumber.setTitleStyle(getStyle(Styles.columnHeader()));
            }
            if (design.getRowNumberStyle() != null) {
                rowNumber.setStyle(getStyle(design.getRowNumberStyle()));
            } else {
                rowNumber.setStyle(getStyle(Styles.column(metadata.isRtl(), Long.class)));
            }
            if (design.getRowNumberWidth() != null) {
                rowNumber.setFixedWidth(design.getRowNumberWidth());
            }
            columns.getColumns().add(rowNumber);
        }

        int colNumber = 1;
        for (String field : metadata.getField()) {
            Optional<Map.Entry<String, Class<?>>> optional = availableFields.entrySet().stream()
                    .filter(e -> Objects.equals(e.getKey(), field)).findAny();
            if (optional.isPresent()) {
                Map.Entry<String, Class<?>> entry = optional.get();
                String name = domainClass.getSimpleName() + "." + entry.getKey();
                ReportColumn reportColumn = design.getColumns().stream()
                        .filter(c -> c.getName().equals(entry.getKey()))
                        .findAny().orElse(new ReportColumn(name));
                String prefix = "";
                if (metadata.getSearch() != null && Stream.of(metadata.getSearch()).anyMatch(s -> s.getField().equals(entry.getKey()))) {
                    prefix = "● ";
                }
                if (metadata.getSort() != null) {
                    Optional<Sort> sort = Stream.of(metadata.getSort()).filter(s -> s.getField().equals(entry.getKey())).findAny();
                    if (sort.isPresent()) {
                        prefix += sort.get().isAsc() ? "↑ " : "↓ ";
                    }
                }
                if (reportColumn.getHeader() == null) {
                    reportColumn.setHeader(prefix + messageResource.getMessageOrDefault(name, ""));
                } else {
                    reportColumn.setHeader(prefix + reportColumn.getHeader());
                }
                Class<?> type = entry.getValue();
                ColumnBuilder<?, ?> col;
                if (type == boolean.class || type == Boolean.class) {
                    BooleanComponentType booleanComponentType = BooleanComponentType.valueOf(messageResource
                            .getMessageOrDefault(reportColumn.getName(), BooleanComponentType.IMAGE_CHECKBOX_1.name()));
                    BooleanColumnBuilder column = DynamicReports.col
                            .booleanColumn(reportColumn.getHeader(), names ? entry.getKey() : "COLUMN_" + colNumber++)
                            .setComponentType(booleanComponentType).setEmptyWhenNullValue(true);
                    if (reportColumn.getWidth() != null) {
                        column.setFixedWidth(reportColumn.getWidth());
                    }
                    col = column;
                } else {
                    TextColumnBuilder<?> column = DynamicReports.col
                            .column(reportColumn.getHeader(), names ? entry.getKey() : "COLUMN_" + colNumber++, getType(entry.getValue()));
                    DateTimeFormatters dateTimeFormatters = metadata.getDateTimeFormatters();
                    if (type == LocalDate.class) {
                        column.setValueFormatter(dateTimeFormatters.dateFormatter());
                    } else if (type == LocalDateTime.class) {
                        column.setValueFormatter(dateTimeFormatters.dateTimeFormatter());
                    } else if (type == LocalTime.class) {
                        column.setValueFormatter(dateTimeFormatters.timeFormatter());
                    } else if (type == Duration.class) {
                        column.setValueFormatter(dateTimeFormatters.durationFormatter());
                    } else if (Enum.class.isAssignableFrom(type) && !EnumValue.class.isAssignableFrom(type)) {
                        column.setValueFormatter(new EnumConverter<>(metadata.getCurrentUser(), messageResource, type));
                    } else {
                        column.setValueFormatter(getConverter(type, converters));
                    }
                    if (reportColumn.getWidth() != null) {
                        column.setFixedWidth(reportColumn.getWidth());
                    }
                    if (reportColumn.getFormat() != null) {
                        column.setPattern(reportColumn.getFormat());
                    }
                    if (reportColumn.isGrouped()) {
                        ColumnGroupBuilder group = DynamicReports.grp.group(column);
                        if (reportColumn.getHeader().isBlank()) {
                            group.setHeaderLayout(GroupHeaderLayout.VALUE);
                        } else {
                            group.setHeaderLayout(GroupHeaderLayout.TITLE_AND_VALUE);
                        }
                        if (reportColumn.getGroupLabelStyle() != null) {
                            group.setTitleStyle(getStyle(reportColumn.getGroupLabelStyle()));
                        } else {
                            group.setTitleStyle(getStyle(Styles.groupLabel()));
                        }
                        if (reportColumn.getGroupStyle() != null) {
                            group.setStyle(getStyle(reportColumn.getGroupStyle()));
                        } else {
                            group.setStyle(getStyle(Styles.group(metadata.isRtl())));
                        }
                        columns.getGroups().add(group);
                    }
                    col = column;
                }
                if (reportColumn.getHeaderStyle() != null) {
                    col.setTitleStyle(getStyle(reportColumn.getHeaderStyle()));
                } else {
                    col.setTitleStyle(getStyle(Styles.columnHeader()));
                }
                if (reportColumn.getStyle() != null) {
                    col.setStyle(getStyle(reportColumn.getStyle()));
                } else {
                    col.setStyle(getStyle(Styles.column(metadata.isRtl(), type)));
                }

                columns.getColumns().add(col);
                if (col instanceof ValueColumnBuilder<?, ?> valueColumn) {
                    columns.getValueColumns().put(field, valueColumn);
                }

                if (reportColumn.getSubtotals() != null) {
                    for (Subtotal subtotal : reportColumn.getSubtotals()) {
                        if (type == boolean.class || type == Boolean.class) {
                            continue;
                        }
                        ValueColumnBuilder<?, ? extends Number> numberColumn = null;
                        if (subtotal.getFunction() != Subtotal.Function.COUNT) {
                            if (!isNumber(type)) {
                                continue;
                            }
                            numberColumn = (ValueColumnBuilder<?, ? extends Number>) col;
                        }
                        AggregationSubtotalBuilder<?> subtotalBuilder = switch (subtotal.getFunction()) {
                            case COUNT -> DynamicReports.sbt.count((ValueColumnBuilder<?, ?>) col);
                            case MIN -> DynamicReports.sbt.min(numberColumn);
                            case MAX -> DynamicReports.sbt.max(numberColumn);
                            case AVG -> DynamicReports.sbt.avg(numberColumn);
                            case SUM -> DynamicReports.sbt.sum(numberColumn);
                        };
                        subtotalBuilder.setLabel(subtotal.getLabel());
                        if (subtotal.getStyle() != null) {
                            subtotalBuilder.setStyle(getStyle(subtotal.getStyle()));
                        }
                        if (subtotal.getLabelStyle() != null) {
                            subtotalBuilder.setLabelStyle(getStyle(subtotal.getLabelStyle()));
                        }
                        switch (subtotal.getPosition()) {
                            case TITLE -> {
                                if (subtotal.getLabelStyle() == null) {
                                    subtotalBuilder.setLabelStyle(getStyle(Styles.titleSubtotalLabel()));
                                }
                                if (subtotal.getStyle() == null) {
                                    subtotalBuilder.setStyle(getStyle(Styles.titleSubtotal()));
                                }
                                columns.getTitleSubtotals().add(subtotalBuilder);
                            }
                            case PAGE_HEADER -> {
                                if (subtotal.getLabelStyle() == null) {
                                    subtotalBuilder.setLabelStyle(getStyle(Styles.pageHeaderSubtotalLabel()));
                                }
                                if (subtotal.getStyle() == null) {
                                    subtotalBuilder.setStyle(getStyle(Styles.pageHeaderSubtotal()));
                                }
                                columns.getPageHeaderSubtotals().add(subtotalBuilder);
                            }
                            case PAGE_FOOTER -> {
                                if (subtotal.getLabelStyle() == null) {
                                    subtotalBuilder.setLabelStyle(getStyle(Styles.pageFooterSubtotalLabel()));
                                }
                                if (subtotal.getStyle() == null) {
                                    subtotalBuilder.setStyle(getStyle(Styles.pageFooterSubtotal()));
                                }
                                columns.getPageFooterSubtotals().add(subtotalBuilder);
                            }
                            case COLUMN_HEADER -> {
                                if (subtotal.getLabelStyle() == null) {
                                    subtotalBuilder.setLabelStyle(getStyle(Styles.columnHeaderSubtotalLabel()));
                                }
                                if (subtotal.getStyle() == null) {
                                    subtotalBuilder.setStyle(getStyle(Styles.columnHeaderSubtotal()));
                                }
                                columns.getColumnHeaderSubtotals().add(subtotalBuilder);
                            }
                            case COLUMN_FOOTER -> {
                                if (subtotal.getLabelStyle() == null) {
                                    subtotalBuilder.setLabelStyle(getStyle(Styles.columnFooterSubtotalLabel()));
                                }
                                if (subtotal.getStyle() == null) {
                                    subtotalBuilder.setStyle(getStyle(Styles.columnFooterSubtotal()));
                                }
                                columns.getColumnFooterSubtotals().add(subtotalBuilder);
                            }
                            default -> {
                                if (subtotal.getLabelStyle() == null) {
                                    subtotalBuilder.setLabelStyle(getStyle(Styles.summarySubtotalLabel()));
                                }
                                if (subtotal.getStyle() == null) {
                                    subtotalBuilder.setStyle(getStyle(Styles.summarySubtotal()));
                                }
                                columns.getSummarySubtotals().add(subtotalBuilder);
                            }
                        }
                    }
                }
            }
        }
        return columns;
    }

    private List<AbstractBaseChartBuilder<?, ?, ?>> getCharts(Map<String, ValueColumnBuilder<?, ?>> columns, List<Chart> charts) {
        List<AbstractBaseChartBuilder<?, ?, ?>> chartBuilders = new ArrayList<>();
        for (Chart chart : charts) {
            AbstractBaseChartBuilder<?, ?, ?> chartBuilder;
            switch (chart) {
                case AreaChart areaChart -> chartBuilder = areaChart.isStacked() ? DynamicReports.cht.stackedAreaChart()
                        .setCategory(getCategory(columns, chart.getCategory()))
                        .series(getSeries(columns, areaChart.getSeries()))
                        .setCategoryAxisFormat(getCategoryAxisFormat(areaChart.getLabel(), areaChart.getLabelFormat()))
                        : DynamicReports.cht.areaChart()
                        .setCategory(getCategory(columns, chart.getCategory()))
                        .series(getSeries(columns, areaChart.getSeries()))
                        .setCategoryAxisFormat(getCategoryAxisFormat(areaChart.getLabel(), areaChart.getLabelFormat()));
                case BarChart barChart -> {
                    if (barChart.isThreeDimension() && barChart.isStacked()) {
                        chartBuilder = DynamicReports.cht.stackedBar3DChart()
                                .setCategory(getCategory(columns, chart.getCategory()))
                                .series(getSeries(columns, barChart.getSeries()))
                                .setCategoryAxisFormat(getCategoryAxisFormat(barChart.getLabel(), barChart.getLabelFormat()));
                    } else if (barChart.isThreeDimension()) {
                        chartBuilder = DynamicReports.cht.bar3DChart()
                                .setCategory(getCategory(columns, chart.getCategory()))
                                .series(getSeries(columns, barChart.getSeries()))
                                .setCategoryAxisFormat(getCategoryAxisFormat(barChart.getLabel(), barChart.getLabelFormat()));
                    } else if (barChart.isStacked()) {
                        chartBuilder = DynamicReports.cht.stackedBarChart()
                                .setCategory(getCategory(columns, chart.getCategory()))
                                .series(getSeries(columns, barChart.getSeries()))
                                .setCategoryAxisFormat(getCategoryAxisFormat(barChart.getLabel(), barChart.getLabelFormat()));
                    } else {
                        chartBuilder = DynamicReports.cht.barChart()
                                .setCategory(getCategory(columns, chart.getCategory()))
                                .series(getSeries(columns, barChart.getSeries()))
                                .setCategoryAxisFormat(getCategoryAxisFormat(barChart.getLabel(), barChart.getLabelFormat()));
                    }
                }
                case LineChart lineChart -> chartBuilder = DynamicReports.cht.lineChart()
                        .setCategory(getCategory(columns, chart.getCategory()))
                        .series(getSeries(columns, lineChart.getSeries()))
                        .setCategoryAxisFormat(getCategoryAxisFormat(lineChart.getLabel(), lineChart.getLabelFormat()));
                case PieChart pieChart -> chartBuilder = pieChart.isThreeDimension() ? DynamicReports.cht.pie3DChart()
                        .series(getSeries(columns, pieChart.getSeries()))
                        .setKey(getCategory(columns, chart.getCategory()))
                        : DynamicReports.cht.pieChart()
                        .series(getSeries(columns, pieChart.getSeries()))
                        .setKey(getCategory(columns, chart.getCategory()));
            }
            chartBuilder.setOrientation(chart.isVertical() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
            if (chart.getTitle() != null) {
                chartBuilder.setTitle(chart.getTitle());
            }
            chartBuilder.setTitleFont(DynamicReports.stl.font()
                    .setFontName(chart.getTitleFormat().getFont().name())
                    .setFontSize(chart.getTitleFormat().getFontSize())
                    .setBold(chart.getTitleFormat().isBold())
                    .setItalic(chart.getTitleFormat().isItalic())
                    .setUnderline(chart.getTitleFormat().isUnderline())
                    .setStrikeThrough(chart.getTitleFormat().isStrikeThrough()));
            chartBuilder.setTitleColor(Color.decode(chart.getTitleFormat().getFontColor()));
            chartBuilders.add(chartBuilder);
        }
        return chartBuilders;
    }

    private AxisFormatBuilder getCategoryAxisFormat(String label, TextFormat labelFormat) {
        AxisFormatBuilder axisFormat = DynamicReports.cht.axisFormat().setLabel(label);
        axisFormat.setLabelFont(DynamicReports.stl.font()
                .setFontName(labelFormat.getFont().name())
                .setFontSize(labelFormat.getFontSize())
                .setBold(labelFormat.isBold())
                .setItalic(labelFormat.isItalic())
                .setUnderline(labelFormat.isUnderline())
                .setStrikeThrough(labelFormat.isStrikeThrough()));
        axisFormat.setLabelColor(Color.decode(labelFormat.getFontColor()));
        return axisFormat;
    }

    private CategoryChartSerieBuilder[] getSeries(Map<String, ValueColumnBuilder<?, ?>> columns, List<String> names) {
        return names.stream().map(name -> getSeries(columns, name)).toArray(CategoryChartSerieBuilder[]::new);
    }

    @SuppressWarnings("unchecked")
    private CategoryChartSerieBuilder getSeries(Map<String, ValueColumnBuilder<?, ?>> columns, String name) {
        return DynamicReports.cht.serie((ValueColumnBuilder<?, ? extends Number>) columns.entrySet().stream()
                .filter(v -> v.getKey().equals(name))
                .filter(v -> Number.class.isAssignableFrom(v.getValue().getColumn().getValueClass()))
                .findAny().orElseThrow(() -> new MetadataFieldAccessException(Number.class, name)).getValue());
    }

    @SuppressWarnings("unchecked")
    private ValueColumnBuilder<?, String> getCategory(Map<String, ValueColumnBuilder<?, ?>> columns, String name) {
        return (ValueColumnBuilder<?, String>) columns.entrySet().stream()
                .filter(v -> v.getKey().equals(name))
                .filter(v -> String.class.isAssignableFrom(v.getValue().getColumn().getValueClass()))
                .findAny().orElseThrow(() -> new MetadataFieldAccessException(String.class, name)).getValue();
    }

    private StringConverter<? extends Comparable<?>> getConverter(Class<?> type, List<StringConverter<? extends Comparable<?>>> converters) {
        return converters.stream().filter(c -> c.isSupported(type)).findFirst().orElse(StringConverter.stringConverter());
    }

    private void mirrorLayout(JasperPrint print) {
        int pageWidth = print.getPageWidth();
        for (JRPrintPage page : print.getPages()) {
            mirrorLayout(page.getElements(), pageWidth);
        }
    }

    private void mirrorLayout(List<JRPrintElement> elements, int width) {
        for (JRPrintElement element : elements) {
            int mirrorX = width - element.getX() - element.getWidth();
            element.setX(mirrorX);
            if (element instanceof JRPrintText printText) {
                printText.setRunDirection(RunDirectionEnum.RTL);
            }
            if (element instanceof JRPrintFrame frame) {
                mirrorLayout(frame.getElements(), frame.getWidth());
            }
        }
    }

    private StyleBuilder getStyle(Style style) {
        StyleBuilder styleBuilder = DynamicReports.stl.style();
        styleBuilder.setFont(DynamicReports.stl.font().setFontName(style.getTextFormat().getFont().name())
                .setFontSize(style.getTextFormat().getFontSize()));
        styleBuilder.setForegroundColor(Color.decode(style.getTextFormat().getFontColor()));
        styleBuilder.setBold(style.getTextFormat().isBold());
        styleBuilder.setItalic(style.getTextFormat().isItalic());
        styleBuilder.setUnderline(style.getTextFormat().isUnderline());
        styleBuilder.setStrikeThrough(style.getTextFormat().isStrikeThrough());
        styleBuilder.setBackgroundColor(Color.decode(style.getBgColor()));
        styleBuilder.setHorizontalTextAlignment(style.getHorizontalAlign());
        styleBuilder.setVerticalTextAlignment(style.getVerticalAlign());

        if (style.getBorderRight() != null) {
            styleBuilder.setRightBorder(getBorder(style.getBorderRight()));
        }
        if (style.getBorderLeft() != null) {
            styleBuilder.setLeftBorder(getBorder(style.getBorderLeft()));
        }
        if (style.getBorderTop() != null) {
            styleBuilder.setTopBorder(getBorder(style.getBorderTop()));
        }
        if (style.getBorderBottom() != null) {
            styleBuilder.setBottomBorder(getBorder(style.getBorderBottom()));
        }

        styleBuilder.setRightPadding(style.getPaddingRight());
        styleBuilder.setLeftPadding(style.getPaddingLeft());
        styleBuilder.setTopPadding(style.getPaddingTop());
        styleBuilder.setBottomPadding(style.getPaddingBottom());

        if (style.getIndentLeft() != null) {
            styleBuilder.setLeftIndent(style.getIndentLeft());
        }
        if (style.getIndentRight() != null) {
            styleBuilder.setRightIndent(style.getIndentRight());
        }

        styleBuilder.setRotation(style.getRotation());
        styleBuilder.setLineSpacing(style.getLineSpacing());

        return styleBuilder;
    }

    private PenBuilder getBorder(Border border) {
        return DynamicReports.stl.pen().setLineWidth(border.getWidth())
                .setLineColor(Color.decode(border.getColor())).setLineStyle(border.getType());
    }

    @Getter
    @Setter
    class Columns {
        private List<ColumnBuilder<?, ?>> columns = new ArrayList<>();
        private Map<String, ValueColumnBuilder<?, ?>> valueColumns = new LinkedHashMap<>();
        private List<AggregationSubtotalBuilder<?>> titleSubtotals = new ArrayList<>();
        private List<AggregationSubtotalBuilder<?>> pageHeaderSubtotals = new ArrayList<>();
        private List<AggregationSubtotalBuilder<?>> pageFooterSubtotals = new ArrayList<>();
        private List<AggregationSubtotalBuilder<?>> columnHeaderSubtotals = new ArrayList<>();
        private List<AggregationSubtotalBuilder<?>> columnFooterSubtotals = new ArrayList<>();
        private List<AggregationSubtotalBuilder<?>> summarySubtotals = new ArrayList<>();
        private List<ColumnGroupBuilder> groups = new ArrayList<>();
    }

}
