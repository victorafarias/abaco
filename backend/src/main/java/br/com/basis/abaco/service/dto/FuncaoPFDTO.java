package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.Complexidade;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author pedro.fernandes
 * @since 28/06/2018
 */
@Getter
@Setter
@NoArgsConstructor
public class FuncaoPFDTO {

    private Long id;
    private Complexidade complexidade;
    private BigDecimal pf;
    private BigDecimal grossPF;

}
