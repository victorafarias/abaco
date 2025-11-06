package br.com.basis.abaco.service.relatorio;

import java.util.List;

import br.com.basis.dynamicexports.constants.DynamicExportsConstants;
import br.com.basis.dynamicexports.pojo.ColunasPropriedadeRelatorio;
import br.com.basis.dynamicexports.pojo.PropriedadesRelatorio;

public class RelatorioAnaliseColunas extends PropriedadesRelatorio {

    private static final String USERS_COLUNA = "users";
    private static final String BLOQUEIA_ANALISE_COLUNA = "bloqueiaAnalise";
    private static final String DATA_CRIACAO_ORDEM_SERVICO_COLUNA = "dataCriacaoOrdemServico";
    private static final String METODO_CONTAGEM_COLUNA = "metodoContagem";
    private static final String BLOQUEIA_STRING = "bloqueiaString";
    private static final String CREATED_ON = "createdOn";
    private static final String ADJUST_PF_TOTAL = "adjustPFTotal";
    private static final String PF_TOTAL = "pfTotal";

    private static final String PF_TOTAL_COLUNA = "pfTotalValor";
    private static final String PF_TOTAL_AJUSTADO_COLUNA = "pfTotalAjustadoValor";
    private static final String PF_TOTAL_APROVADO = "analiseDivergence.pfTotalAprovado";
    private static final String PF_TOTAL_APROVADO_COLUNA = "pfTotalAprovado";

    private static final String METODO_CONTAGEM_STRING = "metodoContagemString";
    private static final String SISTEMA_NOME = "sistema.nome";
    private static final String EQUIPE_RESPONSAVEL_NOME = "equipeResponsavel.nome";
    private static final String IDENTIFICADOR_ANALISE = "identificadorAnalise";
    private static final String ORGANIZACAO_NOME = "organizacao.sigla";
    private static final String NUMERO_OS = "numeroOs";
    private static final String STATUS_NOME = "status.nome";
    private static final String CLONADA_PARA_EQUIPE = "clonadaParaEquipeString";
    private static final String CLONADA_PARA_EQUIPE_COLUNA = "clonadaParaEquipe";

    private static final String ANALISE_CLONADA_PARA_EQUIPE_COLUNA = "analiseClonadaParaEquipe";

    private static final String ANALISE_CLONADA_PARA_EQUIPE = "analiseClonadaParaEquipe.identificadorAnalise";
    private static final String USERS = "nomeUser";

    private static final String ANALISE_DIVERGENCIA_NOME = "analiseDivergence.identificadorAnalise";

    private static final String DATA_HOMOLOGACAO = "dataHomologacao";

    private static final String DATA_ENCERRAMENTO = "dtEncerramento";

    private static final String DATA_HOMOLOGACAO_COLUNA = "dataConclusao";

    private static final String DATA_ENCERRAMENTO_COLUNA = "dataEncerramento";


    private static final String[][] colunasAdicionar = {
        {ORGANIZACAO_NOME, "Organização",ORGANIZACAO_NOME},
        {IDENTIFICADOR_ANALISE,"Identificador",IDENTIFICADOR_ANALISE},
        {ANALISE_DIVERGENCIA_NOME, "Identificador da Divergência", ANALISE_DIVERGENCIA_NOME},
        {EQUIPE_RESPONSAVEL_NOME,"Equipe",EQUIPE_RESPONSAVEL_NOME},
        {SISTEMA_NOME,"Sistema",SISTEMA_NOME},
        {METODO_CONTAGEM_STRING,"Método De Contagem",METODO_CONTAGEM_COLUNA},
        {PF_TOTAL,"PF Total",PF_TOTAL_COLUNA},
        {ADJUST_PF_TOTAL,"PF Ajustado",PF_TOTAL_AJUSTADO_COLUNA},
        {PF_TOTAL_APROVADO, "PF Conciliação", PF_TOTAL_APROVADO_COLUNA},
        {BLOQUEIA_STRING,"Bloqueada?",BLOQUEIA_ANALISE_COLUNA},
        {NUMERO_OS , "Número OS", NUMERO_OS},
        {STATUS_NOME , "Status", STATUS_NOME},
        {CLONADA_PARA_EQUIPE ,"Clonada Para Equipe", CLONADA_PARA_EQUIPE_COLUNA},
        {ANALISE_CLONADA_PARA_EQUIPE, "Análise Relacionada", ANALISE_CLONADA_PARA_EQUIPE_COLUNA},
        {USERS,"Usuários",USERS_COLUNA},
        {CREATED_ON,"Data da Criação",DATA_CRIACAO_ORDEM_SERVICO_COLUNA},
        {DATA_HOMOLOGACAO_COLUNA, "Data de Conclusão/Bloqueio", DATA_HOMOLOGACAO},
        {DATA_ENCERRAMENTO_COLUNA, "Data de Encerramento", DATA_ENCERRAMENTO}
    };


    public RelatorioAnaliseColunas(List<String> colunasVisiveis) {
        super("Listagem das Analises", "Total de Analises");
        for (String[] string : colunasAdicionar) {
            if(colunasVisiveis.contains(string[2])) {
                super.getColunas().add(new ColunasPropriedadeRelatorio(string[0], string[1], String.class, 10, DynamicExportsConstants.MASCARA_NULL, DynamicExportsConstants.ALINHAR_ESQUERDA));
            }
        }
    }
}
