package org.jboss.quickstarts.wfk.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy=CompareStringsValidator.class)
@Documented
public @interface CompareStrings {
    String[] propertyNames();
    StringComparisonMode matchMode() default StringComparisonMode.EQUAL;
    boolean allowNull() default false;
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}