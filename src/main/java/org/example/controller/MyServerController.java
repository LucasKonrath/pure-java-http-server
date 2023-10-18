package org.example.controller;

import org.example.annotations.Controller;
import org.example.annotations.Route;
import org.example.enums.HttpMethod;

import java.util.UUID;

@Controller(context = "/test")
public class MyServerController {

    static MyServerController instance;

    public static MyServerController getInstance(){
        if(instance == null){
            instance = new MyServerController();
        }
        return instance;
    }
    @Route(method = HttpMethod.GET, route = "/uuid")
    public String uuid(){
        return UUID.randomUUID().toString();
    }
}
