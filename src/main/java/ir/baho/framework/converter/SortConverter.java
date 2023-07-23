package ir.baho.framework.converter;

import ir.baho.framework.metadata.Sort;
import org.springframework.core.convert.converter.Converter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SortConverter implements Converter<String, Sort> {

    @Override
    public Sort convert(String sort) {
        if (sort.isBlank()) {
            return new Sort(null, false);
        }
        sort = URLDecoder.decode(sort, StandardCharsets.UTF_8);
        sort = PersianStringConverter.fix(sort);
        int i = sort.indexOf(':');
        if (i == 0) {
            return new Sort(null, false);
        } else if (i > 0) {
            return new Sort(sort.substring(0, i), !sort.substring(i + 1).equalsIgnoreCase("desc"));
        } else {
            return new Sort(sort, true);
        }
    }

}
