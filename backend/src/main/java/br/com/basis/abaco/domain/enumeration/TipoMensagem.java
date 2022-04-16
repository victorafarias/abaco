package br.com.basis.abaco.domain.enumeration;

public enum TipoMensagem {
    SUCESSO,
    AVISO,
    ERRO;

    public Boolean isSucesso(){
        return this.equals(SUCESSO);
    }

    public Boolean isAviso(){
        return this.equals(AVISO);
    }

    public Boolean isErro(){
        return this.equals(ERRO);
    }
}
