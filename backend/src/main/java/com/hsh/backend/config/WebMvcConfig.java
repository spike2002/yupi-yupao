//package com.hsh.backend.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * Web MVC 配置
// */
//@Configuration
//public class WebMvcConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // 映射 Knife4j 文档页面
//        registry.addResourceHandler("/doc.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//
//        // 映射 Knife4j 依赖的公共资源 (webjars)
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//}