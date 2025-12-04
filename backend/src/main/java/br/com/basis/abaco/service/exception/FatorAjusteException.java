package br.com.basis.abaco.service.exception;

import java.util.List;

public class FatorAjusteException extends RuntimeException {

    private final List<String> fatoresNaoEncontrados;

    public FatorAjusteException(List<String> fatoresNaoEncontrados) {
        super("Fatores de ajuste não encontrados");
        this.fatoresNaoEncontrados = fatoresNaoEncontrados;
    }

    public List<String> getFatoresNaoEncontrados() {
        return fatoresNaoEncontrados;
    }
}
