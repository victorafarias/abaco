package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.Manual;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ManualContratoDTO {

    private Long id;
    private ManualDTO manual;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private Boolean ativo;
}
