package com.myspring.demo.annotaiton;

import java.lang.annotation.*;

/**
 * @ClassName: MyController
 * @Description: TODO
 * @Author: niran
 * @Date: 2019/11/7
 **/

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //作用在类上
public @interface MyController {
    String value() default  "";
}
