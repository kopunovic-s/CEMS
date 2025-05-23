package com._project._project.Authentication;

import com._project._project.User.User;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping(path = "/register")
    public User register(@RequestBody RegisterRequest request) {return authenticationService.registerUser(request);}

    @PostMapping(path = "/login")
    public User logIn(@RequestBody LoginRequest request) {return authenticationService.loginUser(request);}
}
