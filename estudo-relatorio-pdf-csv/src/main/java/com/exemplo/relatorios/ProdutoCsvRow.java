package com.exemplo.relatorios;
import com.opencsv.bean.CsvBindByName;
public class ProdutoCsvRow{
    @CsvBindByName(column = "Nome Popular")
    private String nomePopular;
    @CsvBindByName(column = "Tipo")
    private String tipo;
    @CsvBindByName(column = "Espécie")
    private String especie;
    @CsvBindByName(column = "Formato")
    private String formato;
    @CsvBindByName(column = "Data Inclusão")
    private String dataInclusao;

    public String getNomePopular() {
        return nomePopular;
    }
    public String getTipo() {
        return tipo;
    }
    public String getEspecie() {
        return especie;
    }
    public String getFormato() {
        return formato;
    }
    public String getDataInclusao() {
        return dataInclusao;
    }
    public static ProdutoCsvRow from(Produto produto){
        ProdutoCsvRow linha = new ProdutoCsvRow();
        linha.nomePopular = produto.getNomePopular();
        linha.tipo = produto.getTipo();
        linha.especie = produto.getEspecie();
        linha.formato = produto.getFormato();
        linha.dataInclusao = produto.getDataInclusao() != null ? produto.getDataInclusao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        return linha;
    }
}