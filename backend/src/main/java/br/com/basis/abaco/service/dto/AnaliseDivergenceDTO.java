package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.dynamicexports.pojo.ReportObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AnaliseDivergenceDTO implements ReportObject, Serializable {

    private Long id;
    private String identificadorAnalise;
    private String numeroOs;
    private OrganizacaoAnaliseDTO organizacao;
    private TipoEquipeAnaliseDTO equipeResponsavel;
    private SistemaAnaliseDTO sistema;
    private MetodoContagem metodoContagem;
    private BigDecimal pfTotal;
    private BigDecimal adjustPFTotal;
    private String pfTotalAprovado;
    private StatusDTO status;

}
