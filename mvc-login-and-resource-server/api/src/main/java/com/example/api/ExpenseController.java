package com.example.api;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExpenseController {

    @GetMapping(value = "/expenses", produces = "application/json")
    @ResponseBody
    public String getExpenses() {
        return new JSONObject()
                .put("expenses", "You spent one millllion dollars last month!")
                .toString();
    }
}
