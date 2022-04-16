package br.com.basis.abaco.service.dto.novo;


import br.com.basis.abaco.domain.enumeration.TipoMensagem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author pedro.fernandes
 */
@Getter
@Setter
@NoArgsConstructor
public class Mensagem {

    private String mensagem;
    private TipoMensagem tipo;

    public Boolean isSucesso(){
        return tipo.isSucesso();
    }

    public Boolean isAviso(){
        return tipo.isAviso();
    }

    public Boolean isErro(){
        return tipo.isErro();
    }
}
