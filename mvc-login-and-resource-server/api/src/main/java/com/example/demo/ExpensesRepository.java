package com.example.demo;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ExpensesRepository {
    private static List<Expense> expenses = new ArrayList<>();

    static {
        expenses.add(new Expense("client dinner", 175.00, true));
        expenses.add(new Expense("breakfast at airport", 17.34, true));
        expenses.add(new Expense("Lyft to airport", 72.45, true));
        expenses.add(new Expense("Lyft from airport", 63.55, true));
    }

    public Flux<Expense> findAll() {
        return Flux.fromIterable(expenses);
    }
}
