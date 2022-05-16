package br.com.basis.abaco.service.validadores;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.service.dto.novo.AbacoMensagens;

public class AnaliseValidador {

    public AnaliseValidador() {
    }

    public static AbacoMensagens validarAlterarStatus(Long id, Long idStatus, Analise analise, Status status, User user) {
        AbacoMensagens mensagens = new AbacoMensagens();
        if(id == null){
            mensagens.adicionarNovoErro("ID da análise está vázio");
        }
        if(idStatus == null){
            mensagens.adicionarNovoErro("ID do status está vázio");
        }
        if(analise == null){
            mensagens.adicionarNovoErro("Análise não foi encontrada");
        }
        if(status == null){
            mensagens.adicionarNovoErro("Status não foi encontrado");
        }
        if(user == null) {
            mensagens.adicionarNovoErro("Usuário não foi encontrado");
        }

        return mensagens;
    }
}
