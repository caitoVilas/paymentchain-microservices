package org.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.paymentchain.customer.entity.Customer;
import org.paymentchain.customer.entity.CustomerProduct;
import org.paymentchain.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository repository;

    private final WebClient.Builder webClientBuilder;

    public CustomerController(WebClient.Builder webclientBuilder) {
        this.webClientBuilder = webclientBuilder;
    }

    //redefine timeout
    TcpClient tcpClient = TcpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    private <T> List<T> getTransacctions(String accontIban){

        WebClient client = webClientBuilder.clientConnector(
                new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl("http://localhost:8082/transactions")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/transactions"))
                .build();
        List<Object> block = client.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                .path("/transaction")
                .queryParam("ibanAccount", accontIban)
                .build())
                .retrieve().bodyToFlux(Object.class).collectList().block();
        List<T> name = (List<T>) block;
        return name;
    }

    private String getProductName(Long id){

        WebClient client = webClientBuilder.clientConnector(
                new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl("http://localhost:8083/products")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/products"))
                .build();
        JsonNode block = client.method(HttpMethod.GET).uri("/"+id)
                .retrieve().bodyToMono(JsonNode.class).block();
        String name = block.get("name").asText();
        return name;
    }

    @GetMapping("/full")
    public Customer get(@RequestParam String code){
        Customer customer = repository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        products.forEach(dto -> {
            String productName = getProductName(dto.getProductId());
            dto.setProductName(productName);
        });
        customer.setTransactions(getTransacctions(customer.getIban()));
        return customer;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll(){

        List<Customer> customers = repository.findAll();
        if (customers.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long id){

        Customer customer = repository.findById(id).orElse(null);
        if (customer == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer){

        customer.getProducts().forEach(x -> x.setCustomer(customer));
        Customer customerNew = repository.save(customer);
        return ResponseEntity.ok(customerNew);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id){

        Customer customer = repository.findById(id).orElse(null);
        if (customer == null) return ResponseEntity.notFound().build();
        return (ResponseEntity<?>) ResponseEntity.ok();
    }
}
