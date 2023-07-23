package ir.baho.framework.enumeration;

import java.io.Serializable;

public interface EnumValue<E extends Serializable & Comparable<E>> {

    E getValue();

}
