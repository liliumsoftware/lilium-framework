package ir.baho.framework.file;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.MessageResource;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ExcelWriter extends BaseWriter {

    private final boolean rtl;
    private final OutputStream outputStream;
    private final XSSFWorkbook workbook;
    private final Map<Integer, XSSFSheet> sheets;

    public ExcelWriter(boolean rtl, MessageResource messageResource,
                       Collection<StringConverter<?>> converters, OutputStream outputStream) {
        super(messageResource, converters);
        this.rtl = rtl;
        this.outputStream = outputStream;
        this.workbook = new XSSFWorkbook();
        this.sheets = new HashMap<>();
    }

    public ExcelWriter(MessageResource messageResource,
                       Collection<StringConverter<?>> converters, OutputStream outputStream) {
        this(true, messageResource, converters, outputStream);
    }

    public <T> XSSFWorkbook write(Collection<T> list, ExcelSheetWriter<T> sheetWriter) {
        return this.write(list, sheetWriter, 0, null);
    }

    public <T> XSSFWorkbook write(Collection<T> list, ExcelSheetWriter<T> sheetWriter, int sheetIndex) {
        return this.write(list, sheetWriter, sheetIndex, null);
    }

    public <T> XSSFWorkbook write(Collection<T> list, ExcelSheetWriter<T> sheetWriter, String sheetName) {
        return this.write(list, sheetWriter, 0, sheetName);
    }

    public <T> XSSFWorkbook write(Collection<T> list, ExcelSheetWriter<T> sheetWriter, int sheetIndex, String sheetName) {
        XSSFSheet sheet = this.getSheet(this.workbook, sheetIndex, sheetName);
        sheetWriter.write(sheet, list);
        return workbook;
    }

    private XSSFSheet getSheet(XSSFWorkbook workbook, int sheetIndex, String sheetName) {
        XSSFSheet sheet;
        if (this.sheets.containsKey(sheetIndex)) {
            sheet = sheets.get(sheetIndex);
        } else if (sheetIndex == workbook.getNumberOfSheets()) {
            sheet = sheetName == null ? workbook.createSheet() : workbook.createSheet(sheetName);
            this.sheets.put(sheetIndex, sheet);
        } else {
            sheet = workbook.getSheetAt(sheetIndex);
        }
        return sheet;
    }

    @Override
    public void close() throws IOException {
        workbook.write(outputStream);
        workbook.close();
    }

    public abstract class ExcelSheetWriter<T> {

        private final boolean header;
        private final Map<XSSFRow, Integer> columnNumbers;

        public ExcelSheetWriter(boolean header) {
            this.header = header;
            this.columnNumbers = new HashMap<>();
        }

        public ExcelSheetWriter() {
            this(true);
        }

        protected void write(XSSFSheet sheet, Collection<T> list) {
            sheet.setRightToLeft(isRtl(sheet));
            int rowNum = sheet.getPhysicalNumberOfRows();
            if (this.header && rowNum == 0) {
                this.createHeaderRow(sheet, rowNum++);
            }
            for (T item : list) {
                if (!shouldCreateRow(sheet, rowNum, item)) continue;
                this.createRow(sheet, rowNum++, item);
            }
        }

        protected boolean isRtl(XSSFSheet sheet) {
            return rtl;
        }

        protected void createHeaderRow(XSSFSheet sheet, int rowNum) {
            XSSFRow headerRow = sheet.createRow(rowNum);
            this.columnNumbers.put(headerRow, 0);
            this.fillHeaderRow(headerRow);
        }

        protected boolean shouldCreateRow(XSSFSheet sheet, int rowNum, T item) {
            return true;
        }

        protected void createRow(XSSFSheet sheet, int rowNum, T item) {
            XSSFRow row = sheet.createRow(rowNum);
            this.columnNumbers.put(row, 0);
            this.fillRow(row, item);
        }

        protected void createHeaderRow(XSSFRow row, String... headers) {
            for (String header : headers) {
                Integer colNum = this.columnNumbers.get(row);
                XSSFCell cell = row.createCell(colNum, CellType.STRING);
                cell.setCellValue(messageResource.getMessage(header));
                this.columnNumbers.put(row, colNum + 1);
            }
        }

        protected void createRow(XSSFRow row, Object... values) {
            for (Object value : values) {
                Integer colNum = this.columnNumbers.get(row);
                XSSFCell cell = row.createCell(colNum, this.getCellType(value));
                switch (value) {
                    case null -> cell.setCellValue((String) null);
                    case String s -> cell.setCellValue(convert(String.class, value));
                    case Boolean b -> cell.setCellValue(b);
                    case Double v -> cell.setCellValue(v);
                    case Long l -> cell.setCellValue(l);
                    case Integer i -> cell.setCellValue(i);
                    case RichTextString richTextString -> cell.setCellValue(richTextString);
                    case LocalDate localDate -> cell.setCellValue(convert(LocalDate.class, value));
                    case LocalDateTime localDateTime -> cell.setCellValue(convert(LocalDateTime.class, value));
                    case LocalTime localTime -> cell.setCellValue(convert(LocalTime.class, value));
                    case Duration duration -> cell.setCellValue(convert(Duration.class, value));
                    case Enum<?> e -> cell.setCellValue(convert(e.getDeclaringClass(), value));
                    default -> throw new IllegalArgumentException("Type not supported for object: " + value);
                }
                this.columnNumbers.put(row, colNum + 1);
            }
        }

        protected CellType getCellType(Object value) {
            if (value == null) {
                return CellType.BLANK;
            } else if (value instanceof String || value instanceof Character || value instanceof Temporal || value instanceof TemporalAmount) {
                return CellType.STRING;
            } else if (value instanceof Boolean) {
                return CellType.BOOLEAN;
            } else if (value instanceof Number) {
                return CellType.NUMERIC;
            }
            return CellType.BLANK;
        }

        protected void fillHeaderRow(XSSFRow header) {
        }

        protected abstract void fillRow(XSSFRow row, T item);

    }

}
