package ir.baho.framework.converter;

import ir.baho.framework.metadata.Constraint;
import ir.baho.framework.metadata.Search;
import org.springframework.core.convert.converter.Converter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SearchConverter implements Converter<String, Search> {

    private static final String SEPARATOR = "~";
    private static final String ESCAPE = "!";
    private static final String SPLIT_REGEX = "(?<!" + Pattern.quote(ESCAPE) + ")" + Pattern.quote(SEPARATOR);
    private static final String CLEAR_REGEX = Pattern.quote(ESCAPE + SEPARATOR);

    @Override
    public Search convert(String search) {
        if (search.isBlank()) {
            return new Search(null, Constraint.UNKNOWN);
        }
        search = URLDecoder.decode(search, StandardCharsets.UTF_8);
        int i = search.indexOf(':');
        if (i <= 0) {
            return new Search(null, Constraint.UNKNOWN);
        }
        search = PersianStringConverter.fix(search);
        String field = search.substring(0, i);
        String constraintPart = search.substring(i + 1);
        int j = constraintPart.indexOf(':');
        String valuePart = j > 0 ? constraintPart.substring(j + 1) : "";
        String constraintValue = j > 0 ? constraintPart.substring(0, j) : constraintPart;
        Constraint constraint = Constraint.of(constraintValue);

        List<String> valuesPart = Stream.of(valuePart.split(SPLIT_REGEX))
                .map(s -> s.replaceAll(CLEAR_REGEX, SEPARATOR)).filter(v -> !v.isBlank()).toList();

        if (constraint == Constraint.IS_NULL || constraint == Constraint.IS_NOT_NULL) {
            if (valuesPart.isEmpty()) {
                return new Search(field, constraint);
            } else {
                return new Search(null, constraint);
            }
        }

        if (valuesPart.isEmpty()) {
            return new Search(null, constraint);
        }

        if (constraint == Constraint.BETWEEN || constraint == Constraint.NOT_BETWEEN) {
            if (valuesPart.size() == 2) {
                return new Search(field, constraint, valuesPart.get(0), valuesPart.get(1));
            } else {
                return new Search(null, constraint);
            }
        }

        if (constraint == Constraint.IN || constraint == Constraint.NOT_IN) {
            return new Search(field, constraint, new ArrayList<>(valuesPart));
        }

        return new Search(field, constraint, valuesPart.get(0));
    }

}
