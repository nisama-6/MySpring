package com.myspring.demo.service.impl;


import com.myspring.demo.annotaiton.MyAutoWrited;
import com.myspring.demo.annotaiton.MyService;
import com.myspring.demo.model.GetUserInfo;
import com.myspring.demo.service.IHomeService;

@MyService
public class HomeService  implements IHomeService {

    @MyAutoWrited
    private StudentService studentService;

    @Override
    public String sayHi() {
      return   studentService.sayHi();
    }

    @Override
    public String getName(Integer id,String no) {
        return "SB0000"+id;
    }

    @Override
    public String getRequestBody(Integer id, String no, GetUserInfo userInfo) {
        return "userName="+userInfo.getName()+" no="+no;
    }
}
