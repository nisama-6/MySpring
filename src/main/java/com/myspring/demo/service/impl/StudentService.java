package com.myspring.demo.service.impl;


import com.myspring.demo.annotaiton.MyService;
import com.myspring.demo.service.IStudentService;

@MyService
public class StudentService  implements IStudentService {
    @Override
    public String sayHi(){
        return "Hello world!";
    }
}
