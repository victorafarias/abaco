package br.com.basis.abaco.service.dto.upload;

import br.com.basis.abaco.service.dto.AlrDTO;
import br.com.basis.abaco.service.dto.DerDTO;
import br.com.basis.abaco.service.dto.RlrDTO;
import java.util.HashSet;
import java.util.Set;

import br.com.basis.abaco.domain.enumeration.Complexidade;
import br.com.basis.abaco.domain.enumeration.ImpactoFatorAjuste;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO base para funções (dados e transação) durante upload de Excel.
 * Inclui funcionalidade com módulo para serialização completa.
 */
public class FuncaoUploadDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private FuncionalidadeUploadDTO funcionalidade;
    private String tipo;
    private Complexidade complexidade;
    private BigDecimal pf;
    private BigDecimal grossPF;
    private String sustantation;
    private StatusFuncao statusFuncao;
    private ImpactoFatorAjuste impacto;
    private Long ordem;
    private Integer quantidade; // Alterado: Campo adicionado para preservar quantidade durante importação Excel
    private FatorAjusteUploadDTO fatorAjuste;
    
    private Set<DerDTO> ders = new HashSet<>();
    private Set<RlrDTO> rlrs = new HashSet<>();
    private Set<AlrDTO> alrs = new HashSet<>();
    
    public FuncaoUploadDTO() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FuncionalidadeUploadDTO getFuncionalidade() {
        return funcionalidade;
    }
    
    public void setFuncionalidade(FuncionalidadeUploadDTO funcionalidade) {
        this.funcionalidade = funcionalidade;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public Complexidade getComplexidade() {
        return complexidade;
    }
    
    public void setComplexidade(Complexidade complexidade) {
        this.complexidade = complexidade;
    }
    
    public BigDecimal getPf() {
        return pf;
    }
    
    public void setPf(BigDecimal pf) {
        this.pf = pf;
    }
    
    public BigDecimal getGrossPF() {
        return grossPF;
    }
    
    public void setGrossPF(BigDecimal grossPF) {
        this.grossPF = grossPF;
    }
    
    public String getSustantation() {
        return sustantation;
    }
    
    public void setSustantation(String sustantation) {
        this.sustantation = sustantation;
    }
    
    public StatusFuncao getStatusFuncao() {
        return statusFuncao;
    }
    
    public void setStatusFuncao(StatusFuncao statusFuncao) {
        this.statusFuncao = statusFuncao;
    }
    
    public ImpactoFatorAjuste getImpacto() {
        return impacto;
    }
    
    public void setImpacto(ImpactoFatorAjuste impacto) {
        this.impacto = impacto;
    }
    
    public Long getOrdem() {
        return ordem;
    }
    
    public void setOrdem(Long ordem) {
        this.ordem = ordem;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public FatorAjusteUploadDTO getFatorAjuste() {
        return fatorAjuste;
    }

    public void setFatorAjuste(FatorAjusteUploadDTO fatorAjuste) {
        this.fatorAjuste = fatorAjuste;
    }

    public Set<DerDTO> getDers() {
        return ders;
    }

    public void setDers(Set<DerDTO> ders) {
        this.ders = ders;
    }

    public Set<RlrDTO> getRlrs() {
        return rlrs;
    }

    public void setRlrs(Set<RlrDTO> rlrs) {
        this.rlrs = rlrs;
    }

    public Set<AlrDTO> getAlrs() {
        return alrs;
    }

    public void setAlrs(Set<AlrDTO> alrs) {
        this.alrs = alrs;
    }
}
