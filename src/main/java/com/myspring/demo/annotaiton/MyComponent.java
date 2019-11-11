package com.myspring.demo.annotaiton;

import java.lang.annotation.*;

/**
 * @ClassName: MyComponent
 * @Description: TODO
 * @Author: niran
 * @Date: 2019/11/7
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyComponent {
    String value() default "";
}
