package org.paymentchain.transactions.controller;

import org.paymentchain.transactions.entity.Transaction;
import org.paymentchain.transactions.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository repository;

    @GetMapping
    public ResponseEntity<List<Transaction>> getAll(){

        List<Transaction> transactions = repository.findAll();
        if (transactions.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id){
        return repository.findById(id).map(x ->
                ResponseEntity.ok(x)).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction")
    public ResponseEntity<List<Transaction>> get(@RequestParam String ibanAccount){

        List<Transaction> transactions = repository.findByIbanAccount(ibanAccount);
        if (transactions.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction){
        Transaction newTransaction = repository.save(transaction);
        return ResponseEntity.ok(newTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id){

        Transaction transaction = repository.findById(id).orElse(null);
        if (transaction == null) return ResponseEntity.notFound().build();
        return (ResponseEntity<?>) ResponseEntity.ok();
    }
}
