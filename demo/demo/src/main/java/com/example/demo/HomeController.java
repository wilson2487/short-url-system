package com.example.demo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * 首頁重定向
     * 將根路徑重定向到demo1.html頁面
     * 
     * @return String 重定向路徑
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/demo1.html"; // 直接轉向 static/demo.html
    }
}
