package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.FatorAjuste;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.Sistema;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.domain.enumeration.ImpactoFatorAjuste;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoAnalise;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoDados;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoTransacao;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para o modelo EBSERH (modelo 9) da {@link PlanilhaService}.
 *
 * <p>Os testes exercem o caminho público {@code selecionarModelo(analise, 9L)} carregando o template
 * embutido {@code reports/planilhas/modelo9-ebserh.xlsx}, preenchendo uma análise mínima com 1 FD e
 * 1 FT, e verificando as células-chave do resumo ('Info Gerais') e das linhas das funções
 * ('Contagem_PF').</p>
 */
public class PlanilhaServiceEBSERHTest {

    private PlanilhaService planilhaService;

    @Before
    public void setUp() {
        planilhaService = new PlanilhaService();
    }

    @Test
    public void exportarEBSERH_deveGerarPlanilhaComResumoFuncoesDadosETransacao() throws Exception {
        // --- Arrange ---
        Sistema sistema = new Sistema();
        sistema.setNome("Sistema Hospitalar");
        sistema.setSigla("SH");

        Modulo modulo = new Modulo();
        modulo.setNome("Atendimento");

        Funcionalidade funcionalidade = new Funcionalidade();
        funcionalidade.setNome("Triagem");
        funcionalidade.setModulo(modulo);

        FatorAjuste fatorDesenvolvimento = new FatorAjuste();
        fatorDesenvolvimento.setNome("Projeto de Desenvolvimento - Novo");

        FatorAjuste fatorManutencaoInterface = new FatorAjuste();
        fatorManutencaoInterface.setNome("Manutenção em Interface");

        Der der1 = new Der();
        der1.setNome("CPF do paciente");
        Der der2 = new Der();
        der2.setNome("Nome do paciente");

        Rlr rlr1 = new Rlr();
        rlr1.setNome("Paciente");

        FuncaoDados fd = new FuncaoDados();
        fd.setName("Cadastro de Paciente");
        fd.setOrdem(1L);
        fd.setTipo(TipoFuncaoDados.ALI);
        fd.setFuncionalidade(funcionalidade);
        fd.setFatorAjuste(fatorDesenvolvimento);
        fd.setImpacto(ImpactoFatorAjuste.INCLUSAO);
        fd.setDers(new LinkedHashSet<>(Arrays.asList(der1, der2)));
        fd.setRlrs(new LinkedHashSet<>(Collections.singletonList(rlr1)));
        fd.setSustantation(
            "<p>História de Usuário #564987</p>"
            + "<p>HU-564987-Cadastro-Paciente.docx; HU-Anexo.pdf</p>");

        Alr alr1 = new Alr();
        alr1.setNome("Sistema Externo de Triagem");

        Der derFt = new Der();
        derFt.setNome("Sinal vital");

        FuncaoTransacao ft = new FuncaoTransacao();
        ft.setName("Consultar Triagem");
        ft.setOrdem(1L);
        ft.setTipo(TipoFuncaoTransacao.CE);
        ft.setFuncionalidade(funcionalidade);
        ft.setFatorAjuste(fatorManutencaoInterface);
        ft.setImpacto(ImpactoFatorAjuste.ALTERACAO);
        ft.setDers(new LinkedHashSet<>(Collections.singletonList(derFt)));
        ft.setAlrs(new LinkedHashSet<>(Collections.singletonList(alr1)));
        ft.setSustantation(
            "Refere-se à HU #564987 e também à #777111.\n"
            + "Arquivos:\nHU-777111-Consulta.docx\nHU-564987-Cadastro-Paciente.docx");

        User elaborador = new User();
        elaborador.setFirstName("João");
        elaborador.setLastName("Silva");

        Analise analise = new Analise();
        analise.setSistema(sistema);
        analise.setTipoAnalise(TipoAnalise.DESENVOLVIMENTO);
        analise.setMetodoContagem(MetodoContagem.DETALHADA);
        Timestamp dataOS = new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse("2026-05-13").getTime());
        analise.setDataCriacaoOrdemServico(dataOS);
        analise.setUsers(new LinkedHashSet<>(Collections.singletonList(elaborador)));
        analise.setFuncaoDados(new LinkedHashSet<>(Collections.singletonList(fd)));
        analise.setFuncaoTransacao(new LinkedHashSet<>(Collections.singletonList(ft)));

        // --- Act ---
        ByteArrayOutputStream baos = planilhaService.selecionarModelo(analise, 9L);

        // --- Assert ---
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()))) {
            Sheet infoGerais = wb.getSheet("Info Gerais");
            assertThat(infoGerais).as("Aba 'Info Gerais'").isNotNull();
            assertThat(infoGerais.getRow(4).getCell(5).getStringCellValue())
                .isEqualTo("Sistema Hospitalar - SH");
            assertThat(infoGerais.getRow(5).getCell(5).getStringCellValue())
                .isEqualTo("Projeto de Desenvolvimento");
            assertThat(infoGerais.getRow(6).getCell(5).getStringCellValue())
                .isEqualTo("Detalhada (IFPUG)");
            assertThat(infoGerais.getRow(8).getCell(5).getStringCellValue())
                .isEqualTo("João Silva");
            assertThat(infoGerais.getRow(8).getCell(17).getStringCellValue())
                .isEqualTo("13/05/26");

            String huList = infoGerais.getRow(16).getCell(0).getStringCellValue();
            assertThat(huList).contains("História de Usuário 564987");
            assertThat(huList).contains("História de Usuário 777111");
            // Ordem de aparição = FDs primeiro, depois FTs, dedup
            assertThat(huList.indexOf("564987"))
                .isLessThan(huList.indexOf("777111"));

            String arquivosList = infoGerais.getRow(21).getCell(0).getStringCellValue();
            assertThat(arquivosList).contains("HU-564987-Cadastro-Paciente.docx");
            assertThat(arquivosList).contains("HU-Anexo.pdf");
            assertThat(arquivosList).contains("HU-777111-Consulta.docx");

            Sheet contagem = wb.getSheet("Contagem_PF");
            assertThat(contagem).as("Aba 'Contagem_PF'").isNotNull();

            // Linha do FD (índice 7 = linha 8 1-based)
            assertThat(contagem.getRow(7).getCell(0).getStringCellValue())
                .isEqualTo("Atendimento - Triagem - Cadastro de Paciente");
            assertThat(contagem.getRow(7).getCell(1).getStringCellValue()).isEqualTo("ALI");
            assertThat(contagem.getRow(7).getCell(2).getStringCellValue()).isEqualTo("I");
            assertThat((int) contagem.getRow(7).getCell(3).getNumericCellValue()).isEqualTo(2);
            assertThat(contagem.getRow(7).getCell(4).getStringCellValue())
                .contains("CPF do paciente").contains("Nome do paciente");
            assertThat((int) contagem.getRow(7).getCell(5).getNumericCellValue()).isEqualTo(1);
            assertThat(contagem.getRow(7).getCell(6).getStringCellValue()).isEqualTo("Paciente");
            assertThat(contagem.getRow(7).getCell(7).getStringCellValue()).isEqualTo("INCLUSAO");
            assertThat(contagem.getRow(7).getCell(15).getStringCellValue())
                .contains("História de Usuário #564987")
                .contains("HU-564987-Cadastro-Paciente.docx");

            // Linha do FT (logo após o FD, índice 8)
            assertThat(contagem.getRow(8).getCell(0).getStringCellValue())
                .isEqualTo("Atendimento - Triagem - Consultar Triagem");
            // Tipo deve ter sido sobrescrito pelo deflator "Manutenção em Interface" → COSNF
            assertThat(contagem.getRow(8).getCell(1).getStringCellValue()).isEqualTo("COSNF");
            assertThat(contagem.getRow(8).getCell(2).getStringCellValue()).isEqualTo("COSNF");
            assertThat((int) contagem.getRow(8).getCell(3).getNumericCellValue()).isEqualTo(1);
            assertThat(contagem.getRow(8).getCell(4).getStringCellValue()).isEqualTo("Sinal vital");
            assertThat((int) contagem.getRow(8).getCell(5).getNumericCellValue()).isEqualTo(1);
            assertThat(contagem.getRow(8).getCell(6).getStringCellValue())
                .isEqualTo("Sistema Externo de Triagem");
            assertThat(contagem.getRow(8).getCell(7).getStringCellValue()).isEqualTo("ALTERACAO");
        }
    }

    @Test
    public void getTotalDer_comUnicoElementoEValor_deveRetornarValor() {
        Der der = new Der();
        der.setValor(7);
        Integer total = planilhaService.getTotalDer(new LinkedHashSet<>(Collections.singletonList(der)));
        assertThat(total).isEqualTo(7);
    }

    @Test
    public void getTotalDer_comDoisElementos_deveRetornarTamanho() {
        Der d1 = new Der();
        d1.setValor(7); // valor presente, mas como há mais de 1 elemento o size prevalece
        Der d2 = new Der();
        Integer total = planilhaService.getTotalDer(new LinkedHashSet<>(Arrays.asList(d1, d2)));
        assertThat(total).isEqualTo(2);
    }

    @Test
    public void exportarEBSERH_semFuncoes_deveProduzirCelulasResumoEArquivoValido() throws Exception {
        Analise analise = new Analise();
        Sistema sistema = new Sistema();
        sistema.setNome("AbacoApp");
        analise.setSistema(sistema);
        analise.setTipoAnalise(TipoAnalise.APLICACAO);
        analise.setMetodoContagem(MetodoContagem.ESTIMADA);
        analise.setFuncaoDados(Collections.emptySet());
        analise.setFuncaoTransacao(Collections.emptySet());

        ByteArrayOutputStream baos = planilhaService.selecionarModelo(analise, 9L);
        assertThat(baos.size()).isPositive();

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()))) {
            Sheet infoGerais = wb.getSheet("Info Gerais");
            assertThat(infoGerais.getRow(4).getCell(5).getStringCellValue()).isEqualTo("AbacoApp");
            assertThat(infoGerais.getRow(5).getCell(5).getStringCellValue()).isEqualTo("Aplicação");
            assertThat(infoGerais.getRow(6).getCell(5).getStringCellValue()).isEqualTo("Estimativa (NESMA)");
            // Sem funções, os campos (16,0) e (21,0) ficam vazios
            assertThat(infoGerais.getRow(16).getCell(0).getStringCellValue()).isEmpty();
            assertThat(infoGerais.getRow(21).getCell(0).getStringCellValue()).isEmpty();
        }
    }

    @Test
    public void extrairHistoriasDeUsuarioEBSERH_deveProduzirListaUnicaPreservandoOrdem() {
        FuncaoDados fd = new FuncaoDados();
        fd.setSustantation("Texto <b>livre</b> #100 algum trecho #200 e <i>#100</i> repetido");
        FuncaoTransacao ft = new FuncaoTransacao();
        ft.setSustantation("Outra função #300 e #200 referência");

        String resultado = planilhaService.extrairHistoriasDeUsuarioEBSERH(
            Collections.singletonList(fd),
            Collections.singletonList(ft));

        List<String> linhas = Arrays.asList(resultado.split("\n"));
        assertThat(linhas).containsExactly(
            "História de Usuário 100",
            "História de Usuário 200",
            "História de Usuário 300");
    }

    @Test
    public void extrairNomesArquivosHistoriasUsuarioEBSERH_deveSepararPorPontoEVirgulaELinhasEDeduplica() {
        FuncaoDados fd = new FuncaoDados();
        fd.setSustantation("HU-001.docx; HU-002.pdf\nHU-003.xlsx");
        FuncaoTransacao ft = new FuncaoTransacao();
        // "Anotação livre" deve aparecer também, pois Opção A não filtra por extensão;
        // "HU-002.pdf" é repetido e deve ser desduplicado preservando a 1ª aparição.
        ft.setSustantation("Anotação livre\nHU-002.pdf;HU-004.docx");

        String resultado = planilhaService.extrairNomesArquivosHistoriasUsuarioEBSERH(
            Collections.singletonList(fd),
            Collections.singletonList(ft));

        List<String> linhas = Arrays.asList(resultado.split("\n"));
        assertThat(linhas).containsExactly(
            "HU-001.docx",
            "HU-002.pdf",
            "HU-003.xlsx",
            "Anotação livre",
            "HU-004.docx");
    }

    @Test
    public void extrairNomesArquivosHistoriasUsuarioEBSERH_devePreservarQuebrasDeBrEParagrafosHtml() {
        FuncaoDados fd = new FuncaoDados();
        fd.setSustantation("<p>HU-001.docx</p><p>HU-002.pdf</p>");
        FuncaoTransacao ft = new FuncaoTransacao();
        ft.setSustantation("HU-003.xlsx<br/>HU-004.docx");

        String resultado = planilhaService.extrairNomesArquivosHistoriasUsuarioEBSERH(
            Collections.singletonList(fd),
            Collections.singletonList(ft));

        List<String> linhas = Arrays.asList(resultado.split("\n"));
        assertThat(linhas).containsExactly(
            "HU-001.docx",
            "HU-002.pdf",
            "HU-003.xlsx",
            "HU-004.docx");
    }

    @Test
    public void extrairNomesArquivosHistoriasUsuarioEBSERH_semFuncoesOuVazio_deveRetornarStringVazia() {
        FuncaoDados fd = new FuncaoDados();
        fd.setSustantation("   \n;\n;  ");
        String resultado = planilhaService.extrairNomesArquivosHistoriasUsuarioEBSERH(
            Collections.singletonList(fd),
            Collections.emptyList());
        assertThat(resultado).isEmpty();
    }
}
