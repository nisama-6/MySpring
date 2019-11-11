package com.myspring.demo.controller;


import com.myspring.demo.annotaiton.MyAutoWrited;
import com.myspring.demo.annotaiton.MyController;
import com.myspring.demo.annotaiton.MyRequestMapping;
import com.myspring.demo.model.GetUserInfo;
import com.myspring.demo.service.IHomeService;

@MyController
@MyRequestMapping("/home")
public class HomeController {
    @MyAutoWrited
    private IHomeService homeService;

    @MyRequestMapping("/sayHi")
    public String sayHi() {
        return homeService.sayHi();
    }

    @MyRequestMapping("/getName")
    public String getName(Integer id,String no) {
        return homeService.getName(id,no);
    }
    @MyRequestMapping("/getRequestBody")
    public String getRequestBody(Integer id, String no, GetUserInfo userInfo) {
        return homeService.getRequestBody(id,no,userInfo);
    }
}
