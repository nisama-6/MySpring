package com.myspring.demo.annotaiton;

import java.lang.annotation.*;

/**
 * @ClassName: MyAutoWrited
 * @Description: TODO
 * @Author: niran
 * @Date: 2019/11/7
 **/


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //声明作用在字段上
public @interface MyAutoWrited {
    String value() default "";
}
