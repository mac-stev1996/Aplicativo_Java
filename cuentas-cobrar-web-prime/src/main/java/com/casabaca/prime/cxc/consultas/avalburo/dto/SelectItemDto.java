package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class SelectItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String valor;
    private String etiqueta;

    public SelectItemDto() {
    }

    public SelectItemDto(String valor, String etiqueta) {
        this.valor = valor;
        this.etiqueta = etiqueta;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }
}