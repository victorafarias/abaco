package br.com.basis.abaco.utils;

public final class ConfiguracaoUtils {
    private static ConfiguracaoUtils instance;
    private Boolean habilitarCamposFuncao;

    private ConfiguracaoUtils() {
        habilitarCamposFuncao = null;
    }

    public static ConfiguracaoUtils getInstance() {
        if(instance == null) {
            instance = new ConfiguracaoUtils();
        }

        return instance;
    }

    public Boolean getHabilitarCamposFuncao() {
            return habilitarCamposFuncao;
    }

    public void setHabilitarCamposFuncao(Boolean habilitarCamposFuncao) {
        this.habilitarCamposFuncao = habilitarCamposFuncao;
    }
}
