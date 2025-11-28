package br.com.basis.abaco.service.dto.upload;

import java.io.Serializable;

/**
 * DTO para serialização de Módulo durante upload de Excel.
 * Usado para incluir dados transientes (sem ID) do módulo na resposta.
 */
public class ModuloUploadDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nome;
    
    public ModuloUploadDTO() {
    }
    
    public ModuloUploadDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
}
