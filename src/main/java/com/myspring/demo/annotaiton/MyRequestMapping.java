package com.myspring.demo.annotaiton;




import java.lang.annotation.*;

/**
 * @ClassName: MyRequestMapping
 * @Description: TODO
 * @Author: niran
 * @Date: 2019/11/7
 **/


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD}) //作用在类和方法上
public @interface MyRequestMapping {
    String value() default "";
}
