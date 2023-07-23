package ir.baho.framework.repository.specification;

import java.util.Map;

@FunctionalInterface
public interface SimpleSelections {

    Map<String, Class<?>> apply();

}
