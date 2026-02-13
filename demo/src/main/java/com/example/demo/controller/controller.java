package com.example.demo.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test")
@RestController
@RequestMapping("/test")
public class controller {
    @GetMapping("/test")
    public String test(){
        return"Hello world test 345";
    }
}
