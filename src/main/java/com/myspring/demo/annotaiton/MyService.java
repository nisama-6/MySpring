package com.myspring.demo.annotaiton;

import java.lang.annotation.*;

/**
 * @ClassName: MyService
 * @Description: TODO
 * @Author: niran
 * @Date: 2019/11/7
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyService {
    String value() default "";
}