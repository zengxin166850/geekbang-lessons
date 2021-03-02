package org.geektimes.web.mvc.annotation;

import java.lang.annotation.*;

/**
 * 自动注入
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Autowired {

}