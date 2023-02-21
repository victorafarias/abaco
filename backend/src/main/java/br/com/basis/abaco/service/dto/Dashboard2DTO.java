package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.MotivoAnalise;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Dashboard2DTO {

    private MotivoAnalise motivoAnalise;

    private Long contaMotivo;

    private String cliente;

    private Long contaCliente;

    private BigDecimal pfDiferenca;

    private Long totalDemandas;

    private String dataDemanda;

    private BigDecimal pfDiferencaGlobal;

    public Dashboard2DTO(MotivoAnalise motivoAnalise, Long contaMotivo) {
        this.motivoAnalise = motivoAnalise;
        this.contaMotivo = contaMotivo;
    }

    public Dashboard2DTO(String cliente, Long contaCliente) {
        this.cliente = cliente;
        this.contaCliente = contaCliente;
    }

    public Dashboard2DTO(BigDecimal pfDiferenca, String dataDemanda) {
        this.pfDiferenca = pfDiferenca;
        this.dataDemanda = dataDemanda;
    }

    public Dashboard2DTO(Long totalDemandas) {
        this.totalDemandas = totalDemandas;
    }

    public Dashboard2DTO(BigDecimal pfDiferencaGlobal) {
        this.pfDiferencaGlobal = pfDiferencaGlobal;
    }
}
