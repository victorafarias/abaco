package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoAnalise;
import br.com.basis.dynamicexports.pojo.ReportObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class AnaliseDTO implements ReportObject, Serializable {

    private Long id;
    private String identificadorAnalise;
    private String numeroOs;
    private OrganizacaoAnaliseDTO organizacao;
    private Boolean baselineImediatamente;
    private TipoEquipeAnaliseDTO equipeResponsavel;
    private SistemaAnaliseDTO sistema;
    private MetodoContagem metodoContagem;
    private BigDecimal pfTotal;
    private BigDecimal adjustPFTotal;
    private Timestamp dataCriacaoOrdemServico;
    private TipoAnalise tipoAnalise;
    private Boolean isDivergence;
    private boolean bloqueiaAnalise;
    private boolean clonadaParaEquipe;
    private StatusDTO status;
    private AnaliseDivergenceDTO analiseDivergence;
    private BigDecimal pfTotalOriginal;
    private BigDecimal pfTotalAprovado;
    private Double pfTotalValor;
    private Double pfTotalAjustadoValor;
    private String propositoContagem;
    private String escopo;
    private Boolean analiseClonou;
    private Timestamp dataHomologacao;
    private Timestamp dtEncerramento;
    private boolean isEncerrada;

    private Set<AnaliseDivergenceDTO> analisesComparadas = new HashSet<>();
    private Set<UserAnaliseDTO> users = new HashSet<>();
    private Set<CompartilhadaDTO> compartilhadas = new HashSet<>();

    private Set<FuncaoDadosDTO> funcaoDados = new HashSet<>();
    private Set<FuncaoTransacaoDTO> funcaoTransacao = new HashSet<>();

    public Timestamp getDataCriacaoOrdemServico() {
        return ObjectUtils.clone(dataCriacaoOrdemServico);
    }

    public void setDataCriacaoOrdemServico(Timestamp dataCriacaoOrdemServico) {
        this.dataCriacaoOrdemServico = ObjectUtils.clone(dataCriacaoOrdemServico);
    }

    public Timestamp getDataHomologacao() {
        return ObjectUtils.clone(dataHomologacao);
    }

    public void setDataHomologacao(Timestamp dataHomologacao) {
        this.dataHomologacao = ObjectUtils.clone(dataHomologacao);
    }

    public Timestamp getDtEncerramento() {
        return ObjectUtils.clone(dtEncerramento);
    }

    public void setDtEncerramento(Timestamp dtEncerramento) {
        this.dtEncerramento = ObjectUtils.clone(dtEncerramento);
    }
}
