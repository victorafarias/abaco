package br.com.basis.abaco.service.dto.upload;

import java.io.Serializable;

/**
 * DTO para serialização de Funcionalidade durante upload de Excel.
 * Inclui o módulo para garantir que seja serializado corretamente.
 */
public class FuncionalidadeUploadDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nome;
    private ModuloUploadDTO modulo;
    
    public FuncionalidadeUploadDTO() {
    }
    
    public FuncionalidadeUploadDTO(Long id, String nome, ModuloUploadDTO modulo) {
        this.id = id;
        this.nome = nome;
        this.modulo = modulo;
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
    
    public ModuloUploadDTO getModulo() {
        return modulo;
    }
    
    public void setModulo(ModuloUploadDTO modulo) {
        this.modulo = modulo;
    }
}
