package br.com.basis.abaco.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ContratoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String numeroContrato;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private ManualDTO manual;
    private Set<ManualContratoDTO> manualContrato = new HashSet<>();
    private OrganizacaoDTO organization;
    private Boolean ativo;
    private Integer diasDeGarantia;

}
