package com.myspring.demo.service;


import com.myspring.demo.model.GetUserInfo;

public interface IHomeService {
    String sayHi();
    String getName(Integer id, String no);
    String getRequestBody(Integer id, String no, GetUserInfo userInfo);
}
