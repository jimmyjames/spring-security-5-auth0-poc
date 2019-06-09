package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class APIController {

    private final ExpensesRepository expensesRepository;

    public APIController(ExpensesRepository expensesRepository) {
        this.expensesRepository = expensesRepository;
    }

    @GetMapping(value = "/expenses", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Expense> getPrivateExpenses() {
        return expensesRepository.findAll();
    }

}
