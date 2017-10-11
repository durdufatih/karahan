package com.alle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @RequestMapping("/login")
    public ModelAndView hello() {
        ModelAndView modelAndView = new ModelAndView("login");
        return modelAndView;
    }

}
