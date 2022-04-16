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

    private List<Mensagem> mensagens = new ArrayList<>();

    public Boolean contemAviso(){
        for(Mensagem m : mensagens){
            if(m.isAviso()){
                return true;
            }
        }
        return false;
    }

    public Boolean contemErro(){
        for(Mensagem m : mensagens){
            if(m.isErro()){
                return true;
            }
        }
        return false;
    }

    public Boolean contemSucesso(){
        for(Mensagem m : mensagens){
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
        Mensagem mensagemNova = new Mensagem();
        mensagemNova.setMensagem(mensagem);
        mensagemNova.setTipo(TipoMensagem.ERRO);
        mensagens.add(mensagemNova);
        return this;
    }

    public AbacoMensagens adicionarNovoAviso(String mensagem){
        Mensagem mensagemNova = new Mensagem();
        mensagemNova.setMensagem(mensagem);
        mensagemNova.setTipo(TipoMensagem.AVISO);
        mensagens.add(mensagemNova);
        return this;
    }

    public AbacoMensagens adicionarNovoSucesso(String mensagem){
        Mensagem mensagemNova = new Mensagem();
        mensagemNova.setMensagem(mensagem);
        mensagemNova.setTipo(TipoMensagem.SUCESSO);
        mensagens.add(mensagemNova);
        return this;
    }

}
