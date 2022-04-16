package br.com.basis.abaco.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PEAnaliticoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long idfuncaodados;
    private String classificacao;
    private String name;
    private String complexidade;
    private String nomeFuncionalidade;
    private String nomeModulo;
    private Long idFuncionalidade;
    private Long idModulo;
}
