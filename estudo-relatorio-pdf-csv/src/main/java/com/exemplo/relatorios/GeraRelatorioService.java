package com.exemplo.relatorios;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

public class GeraRelatorioService {
    // Gerar CSV
    public String gerarCsv(List<Produto> produtos) {
        List<ProdutoCsvRow> linhas = produtos.stream()
                .map(ProdutoCsvRow::from)
                .collect(Collectors.toList());

        StringWriter writer = new StringWriter();
        try {
            StatefulBeanToCsv<ProdutoCsvRow> beanToCsv = new StatefulBeanToCsvBuilder<ProdutoCsvRow>(writer)
                    .withApplyQuotesToAll(false)
                    .build();
            beanToCsv.write(linhas);
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Erro ao gerar CSV", e);
        }

        return writer.toString();
    }
    //Gerar PDF
        public byte[] gerarPdf(List<Produto> produtos, String titulo) {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, saida);
            document.open();

            Font fonteTitulo = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph paragrafoTitulo = new Paragraph(titulo, fonteTitulo);
            paragrafoTitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(paragrafoTitulo);

            Font fonteData = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.GRAY);
            Paragraph paragrafoData = new Paragraph(
                    "Gerado em: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fonteData);
            paragrafoData.setAlignment(Element.ALIGN_CENTER);
            paragrafoData.setSpacingAfter(20);
            document.add(paragrafoData);

            PdfPTable tabela = new PdfPTable(5);
            tabela.setWidthPercentage(100);

            String[] cabecalhos = {"Nome Popular", "Tipo", "Espécie", "Formato", "Data Inclusão"};
            Font fonteCabecalho = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
            for (String cabecalho : cabecalhos) {
                PdfPCell celula = new PdfPCell(new Phrase(cabecalho, fonteCabecalho));
                celula.setBackgroundColor(new Color(51, 51, 51));
                celula.setPadding(6);
                tabela.addCell(celula);
            }

            for (Produto produto : produtos) {
                tabela.addCell(produto.getNomePopular());
                tabela.addCell(produto.getTipo());
                tabela.addCell(produto.getEspecie());
                tabela.addCell(produto.getFormato());
                String dataInclusao = produto.getDataInclusao() != null
                        ? produto.getDataInclusao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        : "";
                tabela.addCell(dataInclusao);
            }

            document.add(tabela);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

        return saida.toByteArray();
    }
}