package br.com.basis.abaco.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO para requisição de renomeação/alteração de função.
 * Contém os dados necessários para identificar e alterar uma função em todas as suas ocorrências.
 * Os campos "novo*" são opcionais - se null, o campo correspondente não será alterado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenomearFuncaoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Nome do módulo da função a ser alterada (identificação).
     */
    private String nomeModulo;
    
    /**
     * Nome da funcionalidade da função a ser alterada (identificação).
     */
    private String nomeFuncionalidade;
    
    /**
     * Nome atual da função (identificação).
     */
    private String nomeAtual;
    
    /**
     * Novo nome que será atribuído à função (opcional).
     */
    private String novoNome;
    
    /**
     * Novo módulo que será atribuído à função (opcional).
     */
    private String novoModulo;
    
    /**
     * Nova funcionalidade que será atribuída à função (opcional).
     */
    private String novaFuncionalidade;
}

