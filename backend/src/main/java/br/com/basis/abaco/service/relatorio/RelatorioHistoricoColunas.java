package br.com.basis.abaco.service.relatorio;

import br.com.basis.dynamicexports.constants.DynamicExportsConstants;
import br.com.basis.dynamicexports.pojo.ColunasPropriedadeRelatorio;
import br.com.basis.dynamicexports.pojo.PropriedadesRelatorio;

public class RelatorioHistoricoColunas extends PropriedadesRelatorio {

    public RelatorioHistoricoColunas() {
        super("Listagem do Histórico", "Total de Histórico");
        super.getColunas().add(new ColunasPropriedadeRelatorio("somenteData", "Data", String.class, 10, DynamicExportsConstants.MASCARA_NULL, DynamicExportsConstants.ALINHAR_ESQUERDA));
        super.getColunas().add(new ColunasPropriedadeRelatorio("somenteHora", "Hora", String.class, 10, DynamicExportsConstants.MASCARA_NULL, DynamicExportsConstants.ALINHAR_ESQUERDA));
        super.getColunas().add(new ColunasPropriedadeRelatorio("usuario.firstName", "Usuário", String.class, 10, DynamicExportsConstants.MASCARA_NULL, DynamicExportsConstants.ALINHAR_ESQUERDA));
        super.getColunas().add(new ColunasPropriedadeRelatorio("acao", "Ação", String.class, 10, DynamicExportsConstants.MASCARA_NULL, DynamicExportsConstants.ALINHAR_ESQUERDA));
    }
}
