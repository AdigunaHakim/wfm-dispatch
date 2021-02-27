package id.co.asyst.wfm.dispatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/dispatch")
    public String home(){
        return "home";
    }

    @RequestMapping("/dispatch/upload")
    public String upload(){
        return "upload";
    }
}

