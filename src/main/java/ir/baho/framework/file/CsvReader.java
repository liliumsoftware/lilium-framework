package ir.baho.framework.file;

import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class CsvReader<T> implements Closeable {

    protected final CSVParser parser;
    protected final boolean header;

    @SneakyThrows
    public CsvReader(InputStream inputStream, char delimiter, boolean header) {
        this.parser = CSVFormat.EXCEL.builder().setDelimiter(delimiter).get().parse(new InputStreamReader(inputStream));
        this.header = header;
    }

    public CsvReader(InputStream inputStream) {
        this(inputStream, ',', true);
    }

    public List<T> parse() {
        Iterator<CSVRecord> iterator = parser.iterator();
        if (this.header) {
            iterator.next();
        }

        List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            CSVRecord currentRow = iterator.next();
            T t = this.readRow(currentRow);
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    protected T readRow(CSVRecord row) {
        T instance = this.create();
        List<String> cells = row.stream().toList();
        int skips = 0;
        for (int i = 0; i < cells.size(); i++) {
            String cell = cells.get(i);
            if (cell == null || cell.isBlank()) {
                continue;
            }
            boolean read = this.read(i, cell, instance);
            if (!read) {
                skips++;
            }
        }
        if (skips == cells.size()) {
            return null;
        }
        return instance;
    }

    protected abstract T create();

    protected abstract boolean read(int index, String cell, T obj);

    protected boolean readAsBoolean(String cell) {
        return Boolean.parseBoolean(cell);
    }

    protected int readAsInt(String cell) {
        return Double.valueOf(cell.replaceAll(",", "")).intValue();
    }

    protected long readAsLong(String cell) {
        return Double.valueOf(cell.replaceAll(",", "")).longValue();
    }

    protected double readAsDouble(String cell) {
        return Double.parseDouble(cell.replaceAll(",", ""));
    }

    protected BigDecimal readAsBigDecimal(String cell) {
        return new BigDecimal(StringUtils.trimToNull(cell.replaceAll(",", "")));
    }

    protected LocalDateTime readAsDateTime(String cell) {
        return LocalDateTime.parse(cell);
    }

    protected LocalDate readAsDate(String cell) {
        return LocalDate.parse(cell);
    }

    protected LocalTime readAsTime(String cell) {
        return LocalTime.parse(cell);
    }

    protected Duration readAsDuration(String cell) {
        return Duration.parse(cell);
    }

    protected String readAsString(String cell) {
        return StringUtils.trimToNull(cell);
    }

    protected <E extends Enum<E>> E readAsEnum(String cell, Class<E> type) {
        return Stream.of(type.getEnumConstants())
                .filter(e -> e.name().equals(StringUtils.trimToNull(cell)))
                .findAny().orElse(null);
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

}
