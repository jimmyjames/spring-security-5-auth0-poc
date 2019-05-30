package com.auth0.example;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.json.JSONObject;

@Controller
@Component
public class APIController {

    @GetMapping(value = "/api/public", produces = "application/json")
    @ResponseBody
    public String publicEndpoint() {
        return new JSONObject()
                .put("message", "All good. You DO NOT need to be authenticated to call /api/public.")
                .toString();
    }

    @GetMapping(value = "/api/private", produces = "application/json")
    @ResponseBody
    public String privateEndpoint() {
        return new JSONObject()
                .put("message", "All good. You can see this because you are Authenticated.")
                .toString();
    }

    @GetMapping(value = "/api/private-scoped", produces = "application/json")
    @ResponseBody
    public String privateScopedEndpoint() {
        return new JSONObject()
                .put("message", "All good. You can see this because you are Authenticated with a Token granted the 'read:messages' scope")
                .toString();
    }

}
