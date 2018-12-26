package OOP.Tests;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestUtilityClass {
    public static final String[] EMPTY_ARRAY = new String[0];

    public static Object getAnnotationMember(Annotation annotation, String methodName){
        try {
            Method m = annotation.getClass().getDeclaredMethod(methodName);
            return m.invoke(annotation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return "";
    }
}
