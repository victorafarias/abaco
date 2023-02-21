package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoAnalise;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class AnaliseBaseDTO {

    protected Long id;
    protected String numeroOs;
    protected String identificadorAnalise;
    protected OrganizacaoAnaliseDTO organizacao;
    protected TipoEquipeAnaliseDTO equipeResponsavel;
    protected SistemaAnaliseDTO sistema;
    protected MetodoContagem metodoContagem;
    protected BigDecimal pfTotal;
    protected BigDecimal adjustPFTotal;
    protected TipoAnalise tipoAnalise;
    protected Timestamp dataCriacaoOrdemServico;
    protected Boolean fatorCriticidade;
    protected Double valorCriticidade;
    protected Double scopeCreep;

    public void setDataCriacaoOrdemServico(Timestamp dataCriacaoOrdemServico) {
        this.dataCriacaoOrdemServico = dataCriacaoOrdemServico == null ? null : new Timestamp(dataCriacaoOrdemServico.getTime());
    }

    public Timestamp getDataCriacaoOrdemServico() {
        return dataCriacaoOrdemServico == null ? null : new Timestamp(this.dataCriacaoOrdemServico.getTime());
    }
}
