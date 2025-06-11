package com.example.demo.service;

import org.apache.poi.ss.usermodel.Sheet;
import com.example.demo.model.Cliente;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

@Service
public class ClienteService {
    private final String caminhoPlanilha = "clientes.xlsx";

    public Cliente getProximoCliente() throws IOException {
        try (FileInputStream fis = new FileInputStream(caminhoPlanilha);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Clientes");
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // pular cabeçalho

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                return linhaParaCliente(row);
            }

            // se nenhuma linha for encontrada
            throw new NoSuchElementException("Todos os clientes foram tabulados.");
        }
    }


    public void tabularCliente(String cpf, String status) throws IOException {
        FileInputStream fis = new FileInputStream(caminhoPlanilha);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet clientesSheet = workbook.getSheet("Clientes");

        Cliente clienteRemovido = null;
        int rowIndexToRemove = -1;

        for (int i = 1; i <= clientesSheet.getLastRowNum(); i++) {
            Row row = clientesSheet.getRow(i);
            if (row != null && cpf.equals(row.getCell(1).getStringCellValue())) {
                clienteRemovido = linhaParaCliente(row);
                rowIndexToRemove = i;
                break;
            }
        }

        if (clienteRemovido == null) {
            workbook.close();
            throw new RuntimeException("Cliente com CPF " + cpf + " não encontrado.");
        }

        // Remove cliente
        removerLinha(clientesSheet, rowIndexToRemove);

        if (status.equalsIgnoreCase("Não efetivo")) {
            adicionarCliente(clientesSheet, clienteRemovido);
        } else if (status.equalsIgnoreCase("Contato CPC")) {
            adicionarEmAba(workbook, "CPC", clienteRemovido);
        } else if (status.equalsIgnoreCase("Solicitação de Bloqueio")) {
            adicionarEmAba(workbook, "BlackList", clienteRemovido);
        }

        FileOutputStream fos = new FileOutputStream(caminhoPlanilha);
        workbook.write(fos);
        workbook.close();
        fos.close();
    }

    private void adicionarCliente(Sheet sheet, Cliente cliente) {
        int lastRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(lastRow);
        row.createCell(0).setCellValue(cliente.getNome());
        row.createCell(1).setCellValue(cliente.getCpf());
        row.createCell(2).setCellValue(cliente.getTelefone());
    }

    private void adicionarEmAba(Workbook workbook, String nomeAba, Cliente cliente) {
        Sheet sheet = workbook.getSheet(nomeAba);
        if (sheet == null) {
            sheet = workbook.createSheet(nomeAba);
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nome");
            header.createCell(1).setCellValue("CPF");
            header.createCell(2).setCellValue("Telefone");
        }
        adicionarCliente(sheet, cliente);
    }

    private void removerLinha(Sheet sheet, int index) {
        int lastRowNum = sheet.getLastRowNum();
        if (index >= 0 && index < lastRowNum) {
            sheet.shiftRows(index + 1, lastRowNum, -1);
        } else if (index == lastRowNum) {
            sheet.removeRow(sheet.getRow(index));
        }
    }

    private Cliente linhaParaCliente(Row row) {
        Cliente cliente = new Cliente();
        cliente.setNome(row.getCell(0).getStringCellValue());
        cliente.setCpf(row.getCell(1).getStringCellValue());
        cliente.setTelefone(row.getCell(2).getStringCellValue());
        return cliente;
    }
}