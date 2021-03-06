package com.example.back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer{
    
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry reg){
        reg.addResourceHandler("/swagger-ui.html")
         .addResourceLocations("classpath:/META-INF/resources/static");        
    }
    

}
