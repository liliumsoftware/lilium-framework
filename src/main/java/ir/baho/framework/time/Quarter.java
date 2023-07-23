package ir.baho.framework.time;

import ir.baho.framework.enumeration.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Quarter implements EnumValue<Integer> {

    FIRST(1), SECOND(2), THIRD(3), FORTH(4);

    private final Integer value;

}
