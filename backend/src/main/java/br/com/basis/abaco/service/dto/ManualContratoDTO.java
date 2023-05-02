package br.com.basis.abaco.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ManualContratoDTO implements Serializable {

    private Long id;
    private ManualDTO manual;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private Boolean ativo;
}
