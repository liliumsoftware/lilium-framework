package ir.baho.framework.file;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class ExcelReader<T> implements Closeable {

    protected final Workbook workbook;
    protected final boolean header;
    protected final DataFormatter dataFormatter = new DataFormatter();

    @SneakyThrows
    public ExcelReader(InputStream inputStream, boolean header) {
        this.workbook = new XSSFWorkbook(inputStream);
        this.header = header;
    }

    public ExcelReader(InputStream inputStream) {
        this(inputStream, true);
    }

    public List<T> parse(int sheetIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIndex);
        Iterator<Row> iterator = sheet.iterator();
        if (this.header) {
            iterator.next();
        }

        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            list.add(this.readRow(currentRow));
        }
        return list;
    }

    protected T readRow(Row row) {
        T instance = this.create();
        Iterator<Cell> iterator = row.cellIterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            this.read(cell.getColumnIndex(), cell, instance);
        }
        return instance;
    }

    protected abstract T create();

    protected abstract void read(int index, Cell cell, T obj);

    protected boolean readAsBoolean(Cell cell) {
        return cell.getBooleanCellValue();
    }

    protected int readAsInt(Cell cell) {
        return Double.valueOf(cell.getNumericCellValue()).intValue();
    }

    protected long readAsLong(Cell cell) {
        return Double.valueOf(cell.getNumericCellValue()).longValue();
    }

    protected double readAsDouble(Cell cell) {
        return cell.getNumericCellValue();
    }

    protected BigDecimal readAsBigDecimal(Cell cell) {
        return new BigDecimal(StringUtils.trimToNull(dataFormatter.formatCellValue(cell)));//
    }

    protected LocalDateTime readAsDateTime(Cell cell) {
        return cell.getLocalDateTimeCellValue();
    }

    protected LocalDate readAsDate(Cell cell) {
        return cell.getLocalDateTimeCellValue().toLocalDate();
    }

    protected LocalTime readAsTime(Cell cell) {
        return cell.getLocalDateTimeCellValue().toLocalTime();
    }

    protected Duration readAsDuration(Cell cell) {
        return Duration.parse(cell.getStringCellValue());
    }

    protected String readAsString(Cell cell) {
        return StringUtils.trimToNull(dataFormatter.formatCellValue(cell));
    }

    protected <E extends Enum<E>> E readAsEnum(Cell cell, Class<E> type) {
        return Stream.of(type.getEnumConstants())
                .filter(e -> e.name().equals(StringUtils.trimToNull(dataFormatter.formatCellValue(cell))))
                .findAny().orElse(null);
    }

    @Override
    public void close() throws IOException {
        workbook.close();
    }

}
