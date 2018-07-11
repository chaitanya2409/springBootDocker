package com.xxx.b2b.config;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorHandlerController implements ErrorController{

    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error() {
        return "{\"ServiceError\":\"Unable to process your request.\"}";
    }

    public String getErrorPath() {
        return PATH;
    }
}