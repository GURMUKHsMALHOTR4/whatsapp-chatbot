package com.chatbot.whatsappbot.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class RootController {

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.HEAD})
    public String home() {
        return "🚀 WhatsApp Chatbot Backend is live!";
    }
}
