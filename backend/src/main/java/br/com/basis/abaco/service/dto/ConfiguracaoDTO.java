package br.com.basis.abaco.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
public class ConfiguracaoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Boolean habilitarCamposFuncao;
}
