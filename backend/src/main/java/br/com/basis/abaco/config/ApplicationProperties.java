package br.com.basis.abaco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to JHipster.
 * <p>
 * <p>
 * Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String cronAtualizacaoValidacaoDivergencia;

    public String getCronAtualizacaoValidacaoDivergencia() {
        return cronAtualizacaoValidacaoDivergencia;
    }

    public void setCronAtualizacaoValidacaoDivergencia(String cronAtualizacaoValidacaoDivergencia) {
        this.cronAtualizacaoValidacaoDivergencia = cronAtualizacaoValidacaoDivergencia;
    }
}
