package org.geektimes.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * 用于类、接口
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Component {
}
