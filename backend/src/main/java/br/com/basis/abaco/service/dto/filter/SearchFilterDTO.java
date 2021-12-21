package br.com.basis.abaco.service.dto.filter;

import java.util.ArrayList;
import java.util.List;

import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchFilterDTO {
    private String nome;
    private List<String> columnsVisible;

    private List<Funcionalidade> funcionalidades = new ArrayList<>();
    private List<Modulo> modulos = new ArrayList<>();
}
