package com.exemplo.relatorios;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        List<Produto> produtos = List.of(
                new Produto("Ipê Amarelo", "Árvore", "Arvore um", "Muda", LocalDate.of(2024, 3, 10)),
                new Produto("Capim Dourado", "Erva", "Arvore dois", "Semente", LocalDate.of(2024, 5, 22)),
                new Produto("Vitória-Régia", "Aquática", "Arvore tres", "Semente", LocalDate.of(2024, 7, 2))
        );

        GeraRelatorioService service = new GeraRelatorioService();

       
        byte[] pdf = service.gerarPdf(produtos, "Relatório de Produtos");
        Path caminhoPdf = Path.of("relatorio-produtos.pdf");
        Files.write(caminhoPdf, pdf);
        System.out.println("PDF gerado em: " + caminhoPdf.toAbsolutePath());

        // 3. Gera o CSV (String) e salva em disco.
        String csv = service.gerarCsv(produtos);
        Path caminhoCsv = Path.of("relatorio-produtos.csv");
        Files.writeString(caminhoCsv, csv, StandardCharsets.UTF_8);
        System.out.println("CSV gerado em: " + caminhoCsv.toAbsolutePath());

        // Bônus: mostra o conteúdo do CSV direto no console, já que é texto.
        System.out.println("\n--- Conteúdo do CSV ---");
        System.out.println(csv);
    }
}
