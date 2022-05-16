package br.com.basis.abaco.service.dto.formularios;


import br.com.basis.abaco.service.dto.AnaliseEditDTO;
import br.com.basis.abaco.service.dto.novo.AbacoMensagens;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AnaliseFormulario {

    public AnaliseEditDTO analise;
    public AbacoMensagens mensagens;
}
