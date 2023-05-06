package io.blaze.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    Class<?> implementation() default Object.class;

    Scope scope() default Scope.Singleton;

    enum Scope {
        Singleton,
        Prototype
    }
}

