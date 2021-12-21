package br.com.basis.abaco.domain.enumeration;

/**
 * The MotivoAnalise enumeration.
 */
public enum MotivoAnalise {
    CONT_BASIS_MENOR_MAIOR_ERRO_FME(0),
    CONT_BASIS_MENOR_MAIOR_ERRO_BASIS(1),
    CONT_BASIS_MAIOR_MAIOR_ERRO_FME(2),
    CONT_BASIS_MAIOR_MAIOR_ERRO_BASIS(3);

    private Integer numeracao;

    MotivoAnalise(Integer numeracao){
        this.numeracao = numeracao;
    }

    public Integer getNumeracao() {
        return numeracao;
    }

    public void setNumeracao(Integer numeracao) {
        this.numeracao = numeracao;
    }
}
