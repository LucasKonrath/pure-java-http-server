package org.example.controller;

import org.example.annotations.Controller;
import org.example.annotations.PathVariable;
import org.example.annotations.Payload;
import org.example.annotations.Route;
import org.example.enums.HttpMethod;
import org.example.enums.HttpStatus;
import org.example.response.HttpResponse;

import java.util.UUID;

@Controller(context = "/test")
public class MyServerController {

    @Route(method = HttpMethod.GET, route = "/uuid")
    public HttpResponse uuid(){
            return new HttpResponse(UUID.randomUUID().toString(), HttpStatus.OK);
    }

    @Route(method = HttpMethod.GET, route = "/goat/{name}")
//    @Route(method = HttpMethod.GET, route = "/goat/*/add/*")
    // /goat/qlqrcoisa -> matcher exato? ("/goat/qlqrcoisa" salvo)
    // -> se nao tiver dai eu faco o replace de qlqrcoisa por * e pego do map
    // /goat/qlqrcoisa split / == [2]
    // /goat/{name}/add/{outravariavel} = [4]
//    [goat, {name}]
//    [goat, qlqrCoisa]
//    [0] == true
//    [1] == true
    ///goat/qlqrcoisa
    public HttpResponse goat(@PathVariable String name){
        return new HttpResponse(name, HttpStatus.OK);
    }

    @Route(method = HttpMethod.GET, route = "/test")
    public HttpResponse testGet(){
        return new HttpResponse("Get: ", HttpStatus.CREATED);
    }

    @Route(method = HttpMethod.POST, route = "/test")
    public HttpResponse testPost(@Payload String request){
        return new HttpResponse("Post: " + request, HttpStatus.CREATED);
    }

    @Route(method = HttpMethod.PUT, route = "/test")
    public HttpResponse testPut(@Payload String request){
        return new HttpResponse("Put: " + request, HttpStatus.CREATED);
    }

    @Route(method = HttpMethod.DELETE, route = "/test")
    public HttpResponse testDelete(){
        return new HttpResponse("Delete: ", HttpStatus.OK);
    }
}
