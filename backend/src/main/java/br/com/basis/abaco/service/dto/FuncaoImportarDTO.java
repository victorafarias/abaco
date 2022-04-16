package br.com.basis.abaco.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pedro.fernandes
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FuncaoImportarDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    private Long idDeflator;
    private Integer quantidadeINM;
    private String fundamentacao;
    private Long idAnalise;
    private List<PEAnaliticoDTO> funcoesParaImportar = new ArrayList<>();

}
