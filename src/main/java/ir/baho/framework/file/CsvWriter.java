package ir.baho.framework.file;

import ir.baho.framework.converter.StringConverter;
import ir.baho.framework.i18n.MessageResource;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

public abstract class CsvWriter<T> extends BaseWriter {

    private final boolean header;
    private final CSVPrinter printer;

    @SneakyThrows
    public CsvWriter(char delimiter, boolean header, MessageResource messageResource,
                     Collection<StringConverter<?>> converters, OutputStream outputStream) {
        super(messageResource, converters);
        this.header = header;
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write('\ufeff');
        this.printer = new CSVPrinter(writer, CSVFormat.EXCEL.builder().setDelimiter(delimiter).get());
    }

    public CsvWriter(MessageResource messageResource,
                     Collection<StringConverter<?>> converters, OutputStream outputStream) {
        this(',', true, messageResource, converters, outputStream);
    }

    @SneakyThrows
    public CSVPrinter write(Collection<T> list) {
        if (header) {
            fillHeaderRow(printer);
            printer.println();
        }
        for (T t : list) {
            fillRow(printer, t);
            printer.println();
        }
        return printer;
    }

    @SneakyThrows
    protected void createHeaderRow(CSVPrinter printer, String... headers) {
        for (String header : headers) {
            printer.print(messageResource.getMessage(header));
        }
    }

    @SneakyThrows
    protected void createRow(CSVPrinter printer, Object... values) {
        for (Object value : values) {
            if (value == null) {
                printer.print(null);
            } else if (value instanceof String) {
                printer.print(convert(String.class, value));
            } else if (value instanceof Boolean || value instanceof Number) {
                printer.print(value);
            } else if (value instanceof LocalDate) {
                printer.print(convert(LocalDate.class, value));
            } else if (value instanceof LocalDateTime) {
                printer.print(convert(LocalDateTime.class, value));
            } else if (value instanceof LocalTime) {
                printer.print(convert(LocalTime.class, value));
            } else if (value instanceof Duration) {
                printer.print(convert(Duration.class, value));
            } else if (value instanceof Enum<?> e) {
                printer.print(convert(e.getDeclaringClass(), value));
            } else {
                throw new IllegalArgumentException("Type not supported for object: " + value);
            }
        }
    }

    protected void fillHeaderRow(CSVPrinter header) {
    }

    protected abstract void fillRow(CSVPrinter printer, T item);

    @Override
    public void close() throws IOException {
        printer.close();
    }

}
