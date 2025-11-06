package br.com.basis.abaco.jobs;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Organizacao;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoAnalise;
import br.com.basis.abaco.repository.StatusRepository;
import br.com.basis.abaco.service.AnaliseService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JobValidadorDivergencia.class})
public class JobValidadorDivergenciaIT {

    private static final String DEFAULT_NUMERO_OS = "AAAAAAAAAA";
    private static final MetodoContagem DEFAULT_TIPO_CONTAGEM = MetodoContagem.DETALHADA;
    private static final BigDecimal DEFAULT_VALOR_AJUSTE = new BigDecimal(1);
    private static final BigDecimal DEFAULT_PF_TOTAL = new BigDecimal(1);
    private static final String DEFAULT_ESCOPO = "AAAAAAAAAA";
    private static final String DEFAULT_FRONTEIRAS = "AAAAAAAAAA";
    private static final String DEFAULT_DOCUMENTACAO = "AAAAAAAAAA";
    private static final TipoAnalise DEFAULT_TIPO_ANALISE = TipoAnalise.DESENVOLVIMENTO;
    private static final String DEFAULT_PROPOSITO_CONTAGEM = "AAAAAAAAAA";

    @Autowired
    private JobValidadorDivergencia jobValidadorDivergencia;

    @MockBean
    private AnaliseService analiseService;

    @MockBean
    private StatusRepository statusRepository;

    @Test
    public void testeValidadorDivergencia() {
        List<Analise> analises = new ArrayList<>();
        Analise analise = criarAnalise();
        analises.add(analise);
        Mockito.when(analiseService.obterAnalisesDivergenciaForaDoPrazo()).thenReturn(analises);
        Mockito.when(statusRepository.findByNome(Mockito.any())).thenReturn(new Status(1805353L, "Aprovada", true, true));
        this.jobValidadorDivergencia.aprovaDivergenciasForaDoPrazo();
        Assert.assertEquals("Aprovada", analise.getStatus().getNome());
    }

    private Analise criarAnalise() {
        Analise analise = new Analise();
        analise.setNumeroOs(DEFAULT_NUMERO_OS);
        analise.setMetodoContagem(DEFAULT_TIPO_CONTAGEM);
        analise.setValorAjuste(DEFAULT_VALOR_AJUSTE);
        analise.setPfTotal(DEFAULT_PF_TOTAL);
        analise.setEscopo(DEFAULT_ESCOPO);
        analise.setStatus(new Status(1805351L, "Em análise", true, true));
        analise.setDataCriacaoOrdemServico(new Timestamp(new Date(2020, Calendar.FEBRUARY, 1).getTime()));
        analise.setOrganizacao(criarOrganizacao());
        analise.setFronteiras(DEFAULT_FRONTEIRAS);
        analise.setDocumentacao(DEFAULT_DOCUMENTACAO);
        analise.setTipoAnalise(DEFAULT_TIPO_ANALISE);
        analise.setPropositoContagem(DEFAULT_PROPOSITO_CONTAGEM);
        return analise;
    }

    private Organizacao criarOrganizacao() {
        Organizacao organizacao = new Organizacao();
        organizacao.setId(812651L);
        organizacao.setNome("BASIS Tecnologia");
        organizacao.setCnpj("11777162000157");
        organizacao.setAtivo(true);
        organizacao.setNumeroOcorrencia("BASIS_0001");
        organizacao.setSigla("BASIS");
        organizacao.setLogoId(2918702L);
        organizacao.setPrazoAprovacaoDivergenciaDias(60);
        return organizacao;
    }

}
