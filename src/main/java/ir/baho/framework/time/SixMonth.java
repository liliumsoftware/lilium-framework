package ir.baho.framework.time;

import ir.baho.framework.enumeration.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SixMonth implements EnumValue<Integer> {

    FIRST(1), SECOND(2);

    private final Integer value;

}
