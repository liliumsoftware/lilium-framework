package ir.baho.framework.report;

import java.lang.reflect.ParameterizedType;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReportUtils {

    private static final Lock LOCK = new ReentrantLock();
    private static int counter;

    public static String generateUniqueName(String name) {
        try {
            LOCK.lock();
            return generateName(name);
        } finally {
            LOCK.unlock();
        }
    }

    public static Class<?> getGenericClass(Object object, int index) {
        ParameterizedType genericSuperclass = getParameterizedType(object.getClass());
        if (genericSuperclass == null) {
            return String.class;
        }
        Class<?> rawType = getRawType(genericSuperclass.getActualTypeArguments()[index]);
        if (rawType == null) {
            return String.class;
        }
        return rawType;
    }

    private static ParameterizedType getParameterizedType(Class<?> classs) {
        if (classs == null) {
            return null;
        }
        if (classs.getGenericSuperclass() instanceof ParameterizedType) {
            return (ParameterizedType) classs.getGenericSuperclass();
        }
        return getParameterizedType((Class<?>) classs.getGenericSuperclass());
    }

    private static Class<?> getRawType(Object typeArgument) {
        if (typeArgument instanceof ParameterizedType) {
            return getRawType(((ParameterizedType) typeArgument).getRawType());
        } else {
            if (typeArgument instanceof Class<?>) {
                return (Class<?>) typeArgument;
            } else {
                return null;
            }
        }
    }

    private static String generateName(String name) {
        if (counter == Integer.MAX_VALUE) {
            counter = 0;
        }
        return name + "_" + counter++ + "_";
    }

}
