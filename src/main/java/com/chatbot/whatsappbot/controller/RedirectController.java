package com.chatbot.whatsappbot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {

    @RequestMapping(value = "")
    public String redirectToSlash() {
        return "redirect:/";
    }
}
