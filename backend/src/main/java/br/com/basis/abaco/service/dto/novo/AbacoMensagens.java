package br.com.basis.abaco.service.dto.novo;

import br.com.basis.abaco.domain.enumeration.TipoMensagem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pedro.fernandes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbacoMensagens {

    private List<MensagemDTO> mensagens = new ArrayList<>();

    public Boolean contemAviso(){
        for(MensagemDTO m : mensagens){
            if(m.isAviso()){
                return true;
            }
        }
        return false;
    }

    public Boolean contemErro(){
        for(MensagemDTO m : mensagens){
            if(m.isErro()){
                return true;
            }
        }
        return false;
    }

    public Boolean contemSucesso(){
        for(MensagemDTO m : mensagens){
            if(m.isSucesso()){
                return true;
            }
        }
        return false;
    }

    public Boolean estaVazio(){
        return mensagens.isEmpty();
    }

    public Boolean contemAvisoOuErro(){
        return (contemAviso() || contemErro());
    }

    public AbacoMensagens adicionarNovoErro(String mensagem){
        MensagemDTO mensagemDTONova = new MensagemDTO();
        mensagemDTONova.setMensagem(mensagem);
        mensagemDTONova.setTipo(TipoMensagem.ERRO);
        mensagens.add(mensagemDTONova);
        return this;
    }

    public AbacoMensagens adicionarNovoAviso(String mensagem){
        MensagemDTO mensagemDTONova = new MensagemDTO();
        mensagemDTONova.setMensagem(mensagem);
        mensagemDTONova.setTipo(TipoMensagem.AVISO);
        mensagens.add(mensagemDTONova);
        return this;
    }

    public AbacoMensagens adicionarNovoSucesso(String mensagem){
        MensagemDTO mensagemDTONova = new MensagemDTO();
        mensagemDTONova.setMensagem(mensagem);
        mensagemDTONova.setTipo(TipoMensagem.SUCESSO);
        mensagens.add(mensagemDTONova);
        return this;
    }

}
