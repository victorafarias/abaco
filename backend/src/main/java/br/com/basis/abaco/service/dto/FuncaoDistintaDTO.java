package br.com.basis.abaco.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO para representar uma função distinta (combinação única de Módulo, Funcionalidade e Nome da Função).
 * Utilizado na tela de edição de sistemas para exibir funções cadastradas nas análises.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuncaoDistintaDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Nome do módulo ao qual a função pertence.
     */
    private String nomeModulo;
    
    /**
     * Nome da funcionalidade ao qual a função pertence.
     */
    private String nomeFuncionalidade;
    
    /**
     * Nome da função (processo elementar).
     */
    private String nomeFuncao;
    
    /**
     * Tipo da função: "FD" para Função de Dados, "FT" para Função de Transação.
     * Utilizado internamente para saber em qual tabela atualizar.
     */
    private String tipo;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuncaoDistintaDTO that = (FuncaoDistintaDTO) o;
        return Objects.equals(nomeModulo, that.nomeModulo) &&
               Objects.equals(nomeFuncionalidade, that.nomeFuncionalidade) &&
               Objects.equals(nomeFuncao, that.nomeFuncao);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nomeModulo, nomeFuncionalidade, nomeFuncao);
    }
}
