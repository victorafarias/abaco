package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.service.dto.novo.AbacoMensagens;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pedro.fernandes
 * @since 28/06/2018
 */
@Getter
@Setter
@NoArgsConstructor
public class ImportarFDDTO {

    private AbacoMensagens abacoMensagens = new AbacoMensagens();
    private List<FuncaoDados> funcaoDados = new ArrayList<>();

}
