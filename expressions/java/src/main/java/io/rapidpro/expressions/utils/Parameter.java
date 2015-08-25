package io.rapidpro.expressions.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Java 7 compatible replacement for the basic functionality provided by Java 8's Parameter class
 */
public class Parameter {

    protected Class<?> m_type;

    protected Annotation[] m_annotations;

    protected Parameter(Class<?> type, Annotation[] annotations) {
        m_type = type;
        m_annotations = annotations;
    }

    /**
     * Returns an array of Parameter objects that represent all the parameters to the underlying method
     */
    public static Parameter[] fromMethod(Method method) {
        Class<?>[] types = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        int numParams = types.length;
        Parameter[] params = new Parameter[numParams];

        for (int p = 0; p < numParams; p++) {
            params[p] = new Parameter(types[p], annotations[p]);
        }
        return params;
    }

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null
     */
    public <T> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : m_annotations) {
            if (annotation.annotationType().equals(annotationClass)){
                return (T) annotation;
            }
        }
        return null;
    }

    /**
     * Returns annotations that are present on this element
     */
    public Annotation[] getAnnotations() {
        return m_annotations;
    }

    /**
     * Returns a Class object that identifies the declared type for the parameter represented by this Parameter object
     */
    public Class<?> getType() {
        return m_type;
    }
}
