package com.example.demo.controller;

import com.example.demo.model.Cliente;
import com.example.demo.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/proximo")
    public ResponseEntity<?> getProximo() throws IOException {
        try {
            Cliente cliente = clienteService.getProximoCliente();
            return ResponseEntity.ok(cliente);
        } catch (NoSuchElementException e) {
            return ResponseEntity.ok(Map.of("mensagem", "Todos os clientes foram tabulados."));
        }
    }

    @PostMapping("/tabular")
    public String tabular(@RequestBody Map<String, String> payload) throws IOException {
        String cpf = payload.get("cpf");
        String status = payload.get("status");
        clienteService.tabularCliente(cpf, status);
        return "Cliente tabulado com status: " + status;
    }
    @GetMapping("/teste123")
    public String teste123(){
        return "Perdeu!!!";
    }
}