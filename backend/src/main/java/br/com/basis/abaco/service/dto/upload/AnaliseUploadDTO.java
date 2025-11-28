package br.com.basis.abaco.service.dto.upload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para serialização de Análise durante upload de Excel.
 * Usa DTOs específicos que incluem módulo e funcionalidade completos.
 */
public class AnaliseUploadDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String identificadorAnalise;
    private String numeroOs;
    private String metodoContagem;
    private String escopo;
    private String propositoContagem;
    private Boolean bloqueiaAnalise;
    private Boolean enviarBaseline;
    
    private List<FuncaoUploadDTO> funcaoDados = new ArrayList<>();
    private List<FuncaoUploadDTO> funcaoTransacao = new ArrayList<>();
    
    public AnaliseUploadDTO() {
    }
    
    public String getIdentificadorAnalise() {
        return identificadorAnalise;
    }
    
    public void setIdentificadorAnalise(String identificadorAnalise) {
        this.identificadorAnalise = identificadorAnalise;
    }
    
    public String getNumeroOs() {
        return numeroOs;
    }
    
    public void setNumeroOs(String numeroOs) {
        this.numeroOs = numeroOs;
    }
    
    public String getMetodoContagem() {
        return metodoContagem;
    }
    
    public void setMetodoContagem(String metodoContagem) {
        this.metodoContagem = metodoContagem;
    }
    
    public String getEscopo() {
        return escopo;
    }
    
    public void setEscopo(String escopo) {
        this.escopo = escopo;
    }
    
    public String getPropositoContagem() {
        return propositoContagem;
    }
    
    public void setPropositoContagem(String propositoContagem) {
        this.propositoContagem = propositoContagem;
    }
    
    public Boolean getBloqueiaAnalise() {
        return bloqueiaAnalise;
    }
    
    public void setBloqueiaAnalise(Boolean bloqueiaAnalise) {
        this.bloqueiaAnalise = bloqueiaAnalise;
    }
    
    public Boolean getEnviarBaseline() {
        return enviarBaseline;
    }
    
    public void setEnviarBaseline(Boolean enviarBaseline) {
        this.enviarBaseline = enviarBaseline;
    }
    
    public List<FuncaoUploadDTO> getFuncaoDados() {
        return funcaoDados;
    }
    
    public void setFuncaoDados(List<FuncaoUploadDTO> funcaoDados) {
        this.funcaoDados = funcaoDados;
    }
    
    public List<FuncaoUploadDTO> getFuncaoTransacao() {
        return funcaoTransacao;
    }
    
    public void setFuncaoTransacao(List<FuncaoUploadDTO> funcaoTransacao) {
        this.funcaoTransacao = funcaoTransacao;
    }
}
