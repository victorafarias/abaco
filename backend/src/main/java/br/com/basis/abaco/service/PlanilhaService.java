package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.EsforcoFase;
import br.com.basis.abaco.domain.FatorAjuste;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.domain.enumeration.TipoFatorAjuste;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoDados;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoTransacao;
import com.itextpdf.styledxmlparser.jsoup.Jsoup;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing {@link Analise}.
 */
@Service
@Transactional
public class PlanilhaService {

    private final static String ESTIMATIVA = "AFP - Estimativa";
    private final static String DETALHADA = "AFP - Detalhada";
    private final static String RESUMO = "Resumo";
    private final static String PF_POR_FUNCIONALIDADE = "Funcionalidade";
    private final static String SHEET_INM = "AFP - INM";

    private final static String METODO_DETALHADO = "Detalhada";
    private final static String METODO_ESTIMATIVA = "Estimativa";
    private final static String METODO_INDICATIVA = "Indicativa";

    private final static String TIPO_INM = "-------";

    private final static String DIVERGENTE = "Divergente";

    private final static String BASIS_MINUSCULO = "basis";

    private final static Integer QUANTIDADE_SISTEMAS_BNB = 121;

    public ByteArrayOutputStream selecionarModelo(Analise analise, Long modelo) throws IOException {
        List<FuncaoDados> funcaoDadosList = analise.getFuncaoDados().stream().collect(Collectors.toList());
        List<FuncaoTransacao> funcaoTransacaoList = analise.getFuncaoTransacaos().stream().collect(Collectors.toList());
        switch(modelo.intValue()) {
            case 1:
                return this.modeloPadraoBasis(analise, funcaoDadosList, funcaoTransacaoList);
            case 2:
                return this.modeloPadraoBNDES(analise, funcaoDadosList, funcaoTransacaoList);
            case 3:
                return this.modeloPadraoANAC(analise, funcaoDadosList, funcaoTransacaoList);
            case 4:
                // COLOG
                return this.modeloPadraoEB1(analise, funcaoDadosList, funcaoTransacaoList);
            case 5:
                // DCT
                return this.modeloPadraoEB2(analise, funcaoDadosList, funcaoTransacaoList);
            case 6:
                return this.modeloPadraoMCTI(analise, funcaoDadosList, funcaoTransacaoList);
            case 7:
                return this.modeloPadraoBNB(analise, funcaoDadosList, funcaoTransacaoList);
            default:
                return this.modeloPadraoBasis(analise, funcaoDadosList, funcaoTransacaoList);
        }
    }

    private ByteArrayOutputStream modeloPadraoBNB(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo7-bnb.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);
        XSSFFormulaEvaluator xssfFormulaEvaluator = excelFile.getCreationHelper().createFormulaEvaluator();
        xssfFormulaEvaluator.clearAllCachedResultValues();

        this.setarResumoExcelPadraoBNB(excelFile, analise, xssfFormulaEvaluator);
        this.setarFuncoesExcelPadraoBNB(excelFile, funcaoDadosList, funcaoTransacaoList, xssfFormulaEvaluator);
        this.setarFuncoesINMExcelPadraoBNB(excelFile, funcaoTransacaoList, xssfFormulaEvaluator);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }

    private void setarFuncoesINMExcelPadraoBNB(XSSFWorkbook excelFile, List<FuncaoTransacao> funcaoTransacaoList, XSSFFormulaEvaluator xssfFormulaEvaluator) {
        XSSFSheet excelSheet = excelFile.getSheet("Itens Não Mensuráveis");
        xssfFormulaEvaluator.evaluate(excelSheet.getRow(5).getCell(0));
        xssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(5).getCell(0));
        xssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(5).getCell(0));
        int rowNum = 7;
        for(int i = 0; i < funcaoTransacaoList.size(); i++){
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            if(funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)){
                XSSFRow row = excelSheet.getRow(rowNum++);
                String nome = funcaoTransacao.getFuncionalidade().getNome() + " - " + funcaoTransacao.getName();
                row.getCell(0).setCellValue(nome);
                row.getCell(6).setCellValue(funcaoTransacao.getTipo().name());
                row.getCell(8).setCellValue(this.getTotalDer(funcaoTransacao.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoTransacao.getDers())) : "");
                row.getCell(9).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()) != 0 ? String.valueOf(this.getTotalAlr(funcaoTransacao.getAlrs())) : "");
                row.getCell(12).setCellValue(funcaoTransacao.getComplexidade().name());
                Map<String, Double> dadoINM = pegarValorINMBNB(excelFile, funcaoTransacao.getFatorAjuste().getCodigo());
                row.getCell(16).setCellValue(dadoINM.keySet().stream().collect(Collectors.toList()).get(0));
                row.getCell(17).setCellValue(dadoINM.values().stream().collect(Collectors.toList()).get(0));
                row.getCell(18).setCellValue(funcaoTransacao.getQuantidade());
            }
        }

    }

    private Map<String, Double> pegarValorINMBNB(XSSFWorkbook excelFile, String cod) {
        Map<String, Double> dadoINM = new HashMap<>();
        XSSFSheet excelSheet = excelFile.getSheet("INM - Regras");

        for(int i = 23; i < 36; i++){
            XSSFRow row = excelSheet.getRow(i);
            if(row.getCell(2).getStringCellValue().equals(cod)){
                dadoINM.put(row.getCell(3).getStringCellValue(), row.getCell(4).getNumericCellValue());
            }
        }

        return dadoINM;
    }

    private void setarFuncoesExcelPadraoBNB(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, XSSFFormulaEvaluator xssfFormulaEvaluator) {
        XSSFSheet excelSheet = excelFile.getSheet("Funções");
        if(excelSheet.getRow(5).getCell(13).isPartOfArrayFormulaGroup()){
            xssfFormulaEvaluator.evaluate(excelSheet.getRow(5).getCell(13));
            xssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(5).getCell(13));
            xssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(5).getCell(13));
        }

        int rowNum = 7;
        this.setarFuncoesDadosExcelPadraoBNB(funcaoDadosList, excelSheet, xssfFormulaEvaluator, rowNum);
        rowNum += funcaoDadosList.size()+1;
        this.setarFuncoesTransacaoExcelPadraoBNB(funcaoTransacaoList, excelSheet, xssfFormulaEvaluator, rowNum);

    }

    private void setarFuncoesTransacaoExcelPadraoBNB(List<FuncaoTransacao> funcaoTransacaoList, XSSFSheet excelSheet, XSSFFormulaEvaluator xssfFormulaEvaluator, int rowNum) {
        for(int i = 0; i < funcaoTransacaoList.size(); i++){
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            if(!funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)){
                XSSFRow row = excelSheet.getRow(rowNum++);
                String nome = funcaoTransacao.getFuncionalidade().getNome() + " - " + funcaoTransacao.getName();
                row.getCell(0).setCellValue(nome);
                row.getCell(6).setCellValue(funcaoTransacao.getTipo().name());
                row.getCell(7).setCellValue(getImpactoFromFatorAjusteBNB(funcaoTransacao.getFatorAjuste()));
                row.getCell(8).setCellValue(this.getTotalDer(funcaoTransacao.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoTransacao.getDers())) : "");
                row.getCell(9).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()) != 0 ? String.valueOf(this.getTotalAlr(funcaoTransacao.getAlrs())) : "");
                row.getCell(12).setCellValue(funcaoTransacao.getComplexidade().name());
                row.getCell(13).setCellValue(funcaoTransacao.getGrossPF().doubleValue());
                row.getCell(14).setCellValue(funcaoTransacao.getPf().doubleValue());
                row.getCell(17).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
                this.atualizarFormulas(row, 15, xssfFormulaEvaluator);
            }
        }
    }
    private void setarFuncoesDadosExcelPadraoBNB(List<FuncaoDados> funcaoDadosList, XSSFSheet excelSheet, XSSFFormulaEvaluator xssfFormulaEvaluator, int rowNum) {
        for(int i = 0; i < funcaoDadosList.size(); i++){
            XSSFRow row = excelSheet.getRow(rowNum++);
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            String nome = funcaoDados.getFuncionalidade().getNome() + " - " + funcaoDados.getName();
            row.getCell(0).setCellValue(nome);
            row.getCell(6).setCellValue(funcaoDados.getTipo().name());
            row.getCell(7).setCellValue(getImpactoFromFatorAjusteBNB(funcaoDados.getFatorAjuste()));
            row.getCell(8).setCellValue(this.getTotalDer(funcaoDados.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoDados.getDers())) : "");
            row.getCell(9).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()) != 0 ? String.valueOf(this.getTotalRlr(funcaoDados.getRlrs())) : "");
            row.getCell(12).setCellValue(funcaoDados.getComplexidade().name());
            row.getCell(13).setCellValue(funcaoDados.getGrossPF().doubleValue());
            row.getCell(14).setCellValue(funcaoDados.getPf().doubleValue());
            row.getCell(17).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
            this.atualizarFormulas(row, 15, xssfFormulaEvaluator);
        }
    }

    private void atualizarFormulas(XSSFRow row, int i, XSSFFormulaEvaluator xssfFormulaEvaluator){
        for(int j = 0; j < i; j++){
            if(row.getCell(i).isPartOfArrayFormulaGroup()){
                xssfFormulaEvaluator.evaluate(row.getCell(i));
                xssfFormulaEvaluator.evaluateInCell(row.getCell(i));
                xssfFormulaEvaluator.evaluateFormulaCell(row.getCell(i));
            }
        }
    }

    private void setarResumoExcelPadraoBNB(XSSFWorkbook excelFile, Analise analise, XSSFFormulaEvaluator hssfFormulaEvaluator) {
        XSSFSheet excelSheet = excelFile.getSheet("Contagem");
        String nomeElaborador = analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto();
        excelSheet.getRow(4).getCell(5).setCellValue(this.pegarNomeSistemaBNB(excelFile, analise.getSistema().getSigla()));
        excelSheet.getRow(6).getCell(5).setCellValue(nomeElaborador);
        excelSheet.getRow(6).getCell(25).setCellValue(analise.getDataCriacaoOrdemServico());
        if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
            excelSheet.getRow(9).getCell(11).setCellValue("X");
        }

        switch (analise.getTipoAnalise()){
            case DESENVOLVIMENTO:
                excelSheet.getRow(10).getCell(11).setCellValue("X");
                break;
            case MELHORIA:
                excelSheet.getRow(11).getCell(11).setCellValue("X");
                break;
            case APLICACAO:
                excelSheet.getRow(12).getCell(11).setCellValue("X");
                break;
        }

        excelSheet.getRow(16).getCell(0).setCellValue(analise.getPropositoContagem());
        excelSheet.getRow(40).getCell(0).setCellValue(analise.getEscopo());
    }

    private String pegarNomeSistemaBNB(XSSFWorkbook excelFile, String nome) {
        XSSFSheet excelSheet = excelFile.getSheet("Sistemas");
        for(int i = 0; i < QUANTIDADE_SISTEMAS_BNB; i++){
            String nomeSistema = excelSheet.getRow(i).getCell(0).getStringCellValue();
            String nomeDividido = nomeSistema.substring(0,4);
            if(nomeDividido.equals(nome)){
                return nomeSistema;
            }
        }
        return "";
    }

    private String getImpactoFromFatorAjusteBNB(FatorAjuste fatorAjuste){
        switch(fatorAjuste.getFator().intValue()){
            case 100:
                return "I";
            case 50:
                return "A";
            case 30:
                return "E";
            case 15:
                return "T";
            default:
                return "";
        }
    }

    private ByteArrayOutputStream modeloPadraoMCTI(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo6-mcti.xls");
        HSSFWorkbook excelFile = new HSSFWorkbook(stream);
        HSSFFormulaEvaluator hssfFormulaEvaluator = excelFile.getCreationHelper().createFormulaEvaluator();
        hssfFormulaEvaluator.clearAllCachedResultValues();
        this.setarResumoExcelPadraoMCTI(excelFile, analise, hssfFormulaEvaluator);
        this.setarFuncoesDadosExcelPadraoMCTI(excelFile, funcaoDadosList, hssfFormulaEvaluator);
        this.setarFuncoesTransacaoExcelPadraoMCTI(excelFile, funcaoTransacaoList, hssfFormulaEvaluator);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }


    private void setarFuncoesTransacaoExcelPadraoMCTI(HSSFWorkbook excelFile, List<FuncaoTransacao> funcaoTransacaoList, HSSFFormulaEvaluator hssfFormulaEvaluator) {
        HSSFSheet excelSheet = excelFile.getSheet("Funções Transação");

        hssfFormulaEvaluator.evaluate(excelSheet.getRow(3).getCell(1));
        hssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(3).getCell(1));
        hssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(3).getCell(1));

        int rowNum = 8;
        for (FuncaoTransacao funcaoTransacao : funcaoTransacaoList) {
            HSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(String.format("%s - %s", funcaoTransacao.getFuncionalidade().getModulo().getNome(), funcaoTransacao.getFuncionalidade().getNome()));
            row.getCell(1).setCellValue(funcaoTransacao.getName());
            row.getCell(2).setCellValue(funcaoTransacao.getFatorAjuste().getSigla());
            row.getCell(3).setCellValue(funcaoTransacao.getTipo().name());
            row.getCell(4).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()));
            row.getCell(6).setCellValue(this.getTotalDer(funcaoTransacao.getDers()));
            String alrs = funcaoTransacao.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(5).setCellValue(alrs.equals("null") ?  "" : alrs);
            String ders = funcaoTransacao.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(7).setCellValue(ders.equals("null") ? "" : ders);
            row.getCell(10).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
            for(int i = 0; i < 10; i++){
                if(row.getCell(i).isPartOfArrayFormulaGroup()){
                    hssfFormulaEvaluator.evaluate(row.getCell(i));
                    hssfFormulaEvaluator.evaluateInCell(row.getCell(i));
                    hssfFormulaEvaluator.evaluateFormulaCell(row.getCell(i));
                }
            }
        }
    }

    private void setarFuncoesDadosExcelPadraoMCTI(HSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, HSSFFormulaEvaluator hssfFormulaEvaluator) {
        HSSFSheet excelSheet = excelFile.getSheet("Funções Dados");
        hssfFormulaEvaluator.evaluate(excelSheet.getRow(3).getCell(1));
        hssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(3).getCell(1));
        hssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(3).getCell(1));

        int rowNum = 8;
        for (FuncaoDados funcaoDados : funcaoDadosList) {
            HSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(String.format("%s - %s", funcaoDados.getFuncionalidade().getModulo().getNome(), funcaoDados.getFuncionalidade().getNome()));
            row.getCell(1).setCellValue(funcaoDados.getName());
            row.getCell(2).setCellValue(funcaoDados.getFatorAjuste().getSigla());
            row.getCell(3).setCellValue(funcaoDados.getTipo().equals(TipoFuncaoDados.INM) ? "Codedata" : funcaoDados.getTipo().name());
            row.getCell(4).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()));
            row.getCell(6).setCellValue(this.getTotalDer(funcaoDados.getDers()));
            String rlrs = funcaoDados.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(5).setCellValue(rlrs);
            String ders = funcaoDados.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(7).setCellValue(ders);
            row.getCell(10).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
            for(int i = 0; i < 10; i++){
                if(row.getCell(i).isPartOfArrayFormulaGroup()){
                    hssfFormulaEvaluator.evaluate(row.getCell(i));
                    hssfFormulaEvaluator.evaluateInCell(row.getCell(i));
                    hssfFormulaEvaluator.evaluateFormulaCell(row.getCell(i));
                }
            }
        }
    }

    private void setarResumoExcelPadraoMCTI(HSSFWorkbook excelFile, Analise analise, HSSFFormulaEvaluator hssfFormulaEvaluator) {
        HSSFSheet excelSheet = excelFile.getSheet("Capa");

        excelSheet.getRow(4).getCell(0).setCellValue(analise.getSistema().getSigla());
        excelSheet.getRow(4).getCell(3).setCellValue(analise.getNumeroOs() != null ? analise.getNumeroOs() : analise.getIdentificadorAnalise());

        if(analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
            excelSheet.getRow(7).getCell(0).setCellValue("X");
            hssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(7).getCell(0));
            hssfFormulaEvaluator.evaluate(excelSheet.getRow(7).getCell(0));
            hssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(7).getCell(0));
        }else{
            excelSheet.getRow(6).getCell(0).setCellValue("X");
            hssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(6).getCell(0));
            hssfFormulaEvaluator.evaluate(excelSheet.getRow(6).getCell(0));
            hssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(6).getCell(0));
        }

        excelSheet.getRow(6).getCell(2).setCellValue("X");
        excelSheet.getRow(9).getCell(0).setCellValue("X");
        hssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(6).getCell(2));
        hssfFormulaEvaluator.evaluate(excelSheet.getRow(6).getCell(2));
        hssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(6).getCell(2));
        hssfFormulaEvaluator.evaluateInCell(excelSheet.getRow(9).getCell(0));
        hssfFormulaEvaluator.evaluate(excelSheet.getRow(9).getCell(0));
        hssfFormulaEvaluator.evaluateFormulaCell(excelSheet.getRow(9).getCell(0));


        excelSheet.getRow(46).getCell(0).setCellValue(String.format("%s %s %s", analise.getEscopo(), analise.getPropositoContagem(), analise.getObservacoes() != null ? analise.getObservacoes() : ""));

    }

    //EB 2

    private ByteArrayOutputStream modeloPadraoEB2(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo5-eb2.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);

        this.setarResumoExcelPadraoEB2(excelFile, analise);
        this.setarDeflatoresExcelPadraoEB2(excelFile, analise);
        if(!analise.getMetodoContagem().equals(MetodoContagem.INDICATIVA)){
            this.setarFuncoesINMExcelPadraoEB2(excelFile, funcaoTransacaoList);
            if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
                this.setarFuncoesEstimadaExcelPadraoEB2(excelFile, funcaoDadosList, funcaoTransacaoList, analise);
            }else{
                this.setarFuncoesDetalhadaExcelPadraoEB2(excelFile, funcaoDadosList, funcaoTransacaoList, analise);
            }
        }

        this.setarPFPorFuncionalidade(excelFile, funcaoDadosList, funcaoTransacaoList);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }

    private void setarFuncoesEstimadaExcelPadraoEB2(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(ESTIMATIVA);
        excelSheet.getRow(2).getCell(7).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());
        excelSheet.getRow(3).getCell(7).setCellValue(analise.getDataCriacaoOrdemServico());
        int rowNum = 10;
        int idFuncao = 1;
        for(int i = 0; i < funcaoDadosList.size(); i++){
            FuncaoDados funcaoDados =  funcaoDadosList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(5).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(6).setCellValue(funcaoDados.getName());
            row.getCell(7).setCellValue(funcaoDados.getTipo().toString());
            row.getCell(0).setCellValue(idFuncao++);
            row.getCell(1).setCellValue(funcaoDados.getFatorAjuste().getNome());
            row.getCell(4).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(10).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
        }
        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            if (!funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)) {
                XSSFRow row = excelSheet.getRow(rowNum++);
                row.getCell(0).setCellValue(idFuncao++);
                row.getCell(6).setCellValue(funcaoTransacao.getName());
                row.getCell(7).setCellValue(funcaoTransacao.getTipo().toString());
                row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
                row.getCell(4).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
                row.getCell(5).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
                row.getCell(10).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
            }
        }
    }

    private void setarFuncoesDetalhadaExcelPadraoEB2(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);
        excelSheet.getRow(2).getCell(13).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());
        excelSheet.getRow(3).getCell(13).setCellValue(analise.getDataCriacaoOrdemServico());
        int idFuncao = 1;
        int rowNum = 9;
        for(int i = 0; i < funcaoDadosList.size(); i++){
            XSSFRow row = excelSheet.getRow(rowNum++);
            FuncaoDados funcaoDados =  funcaoDadosList.get(i);
            row.getCell(0).setCellValue(idFuncao++);
            row.getCell(1).setCellValue(funcaoDados.getFatorAjuste().getNome());
            row.getCell(3).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(4).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(5).setCellValue(funcaoDados.getName());
            row.getCell(6).setCellValue(funcaoDados.getTipo().toString());
            row.getCell(7).setCellValue(this.getTotalDer(funcaoDados.getDers()));
            String ders = funcaoDados.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(8).setCellValue(ders);
            row.getCell(9).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()));
            String rlrs = funcaoDados.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(10).setCellValue(rlrs);
            row.getCell(18).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
        }
        for(int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            if (!funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)) {
                XSSFRow row = excelSheet.getRow(rowNum++);
                row.getCell(0).setCellValue(idFuncao++);
                row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
                row.getCell(3).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
                row.getCell(4).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
                row.getCell(5).setCellValue(funcaoTransacao.getName());
                row.getCell(6).setCellValue(funcaoTransacao.getTipo().toString());
                row.getCell(7).setCellValue(this.getTotalDer(funcaoTransacao.getDers()));
                String ders = funcaoTransacao.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                row.getCell(8).setCellValue(ders);
                row.getCell(9).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()));
                String alrs = funcaoTransacao.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                row.getCell(10).setCellValue(alrs);
                row.getCell(18).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
            }
        }
    }

    private void setarFuncoesINMExcelPadraoEB2(XSSFWorkbook excelFile, List<FuncaoTransacao> funcaoTransacaoList) {
        XSSFSheet excelSheet = excelFile.getSheet(SHEET_INM);
        int rowNum = 9;
        int idFuncao = 1;
        if(!funcaoTransacaoList.isEmpty()){
            for (int i = 0; i<funcaoTransacaoList.size(); i++){
                FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
                if(funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)){
                    XSSFRow row = excelSheet.getRow(rowNum++);
                    row.getCell(0).setCellValue(idFuncao++);
                    row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
                    row.getCell(3).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
                    row.getCell(4).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
                    row.getCell(5).setCellValue(funcaoTransacao.getName());
                    row.getCell(7).setCellValue(funcaoTransacao.getQuantidade() != null ? funcaoTransacao.getQuantidade() : Integer.valueOf(0));
                    row.getCell(9).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
                }
            }
        }
    }

    private void setarDeflatoresExcelPadraoEB2(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet("Manual EB");
        int rowNum = 2;
        int rowNumUnitario = 2;
        List<FatorAjuste> fatorAjusteList = analise.getManual().getFatoresAjuste().stream().collect(Collectors.toList());
        for(int i = 0; i < fatorAjusteList.size(); i++) {
            FatorAjuste fatorAjuste = fatorAjusteList.get(i);
            if(fatorAjuste.getTipoAjuste().equals(TipoFatorAjuste.PERCENTUAL)){
                XSSFRow row = excelSheet.getRow(rowNum++);
                row.getCell(0).setCellValue(fatorAjuste.getNome());
                row.getCell(1).setCellValue(fatorAjuste.getFator().doubleValue()/100);
            }else{
                XSSFRow row = excelSheet.getRow(rowNumUnitario++);
                row.getCell(9).setCellValue(fatorAjuste.getNome());
                row.getCell(10).setCellValue(fatorAjuste.getFator().doubleValue());
                row.getCell(11).setCellValue("Q");
            }
        }
    }

    private void setarResumoExcelPadraoEB2(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(RESUMO);
        switch(analise.getMetodoContagem()){
            case DETALHADA:
                excelSheet.getRow(3).getCell(1).setCellValue(METODO_DETALHADO);
                break;
            case ESTIMADA:
                excelSheet.getRow(3).getCell(1).setCellValue(METODO_ESTIMATIVA);
                break;
            case INDICATIVA:
                excelSheet.getRow(3).getCell(1).setCellValue(METODO_INDICATIVA);
                break;
        }
        if(analise.getSistema() != null){
            excelSheet.getRow(3).getCell(4).setCellValue(analise.getSistema().getNome());
        }
        if(analise.getNumeroOs() != null){
            excelSheet.getRow(6).getCell(4).setCellValue(analise.getNumeroOs());
        }else{
            excelSheet.getRow(6).getCell(4).setCellValue(analise.getIdentificadorAnalise());
        }
        excelSheet.getRow(8).getCell(4).setCellValue(analise.getDataCriacaoOrdemServico());
        excelSheet.getRow(13).getCell(0).setCellValue(analise.getEscopo());
        excelSheet.getRow(7).getCell(4).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());

    }

    //EB 1 COLOG

    private ByteArrayOutputStream modeloPadraoEB1(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo4-eb1.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);

        this.setarResumoExcelPadraoEB1(excelFile, analise);
        this.setarDeflatoresExcelPadraoEB1(excelFile, analise);
        if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
            this.setarFuncoesEstimadaExcelPadraoEB1(excelFile, funcaoDadosList, funcaoTransacaoList, analise);
        }else{
            this.setarFuncoesDetalhadaExcelPadraoEB1(excelFile, funcaoDadosList, funcaoTransacaoList, analise);
        }
        this.setarPFPorFuncionalidade(excelFile, funcaoDadosList, funcaoTransacaoList);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }

    private void setarFuncoesDetalhadaExcelPadraoEB1(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);

        excelSheet.getRow(2).getCell(15).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());
        excelSheet.getRow(3).getCell(15).setCellValue(analise.getDataCriacaoOrdemServico());

        int rowNum = 9;
        int idFuncao = 1;
        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(8).setCellValue(funcaoDados.getQuantidade() != null ? funcaoDados.getQuantidade() : Integer.valueOf(0));
            row.getCell(9).setCellValue(funcaoDados.getTipo().toString());row.getCell(0).setCellValue(idFuncao++);
            row.getCell(1).setCellValue(funcaoDados.getFatorAjuste().getNome());
            row.getCell(5).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(6).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(7).setCellValue(funcaoDados.getName());
            row.getCell(10).setCellValue(this.getTotalDer(funcaoDados.getDers()));
            row.getCell(11).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()));
            row.getCell(19).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
        }
        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(idFuncao++);
            row.getCell(8).setCellValue(funcaoTransacao.getQuantidade() != null ? funcaoTransacao.getQuantidade() : Integer.valueOf(0));
            row.getCell(9).setCellValue(funcaoTransacao.getTipo().toString());
            row.getCell(10).setCellValue(this.getTotalDer(funcaoTransacao.getDers()));
            row.getCell(11).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()));
            row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
            row.getCell(5).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
            row.getCell(6).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
            row.getCell(7).setCellValue(funcaoTransacao.getName());
            row.getCell(19).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
        }
    }

    private void setarFuncoesEstimadaExcelPadraoEB1(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(ESTIMATIVA);
        excelSheet.getRow(2).getCell(10).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());
        excelSheet.getRow(3).getCell(10).setCellValue(analise.getDataCriacaoOrdemServico());
        int rowNum = 9;
        int idFuncao = 1;
        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(idFuncao++);
            row.getCell(1).setCellValue(funcaoDados.getFatorAjuste().getNome());
            row.getCell(5).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(6).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(7).setCellValue(funcaoDados.getName());
            row.getCell(8).setCellValue(funcaoDados.getQuantidade() != null ? funcaoDados.getQuantidade() : Integer.valueOf(0));
            row.getCell(9).setCellValue(funcaoDados.getTipo().toString());
            row.getCell(11).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
        }
        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(idFuncao++);
            row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
            row.getCell(5).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
            row.getCell(6).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
            row.getCell(7).setCellValue(funcaoTransacao.getName());
            row.getCell(8).setCellValue(funcaoTransacao.getQuantidade() != null ? funcaoTransacao.getQuantidade() : Integer.valueOf(0));
            row.getCell(9).setCellValue(funcaoTransacao.getTipo().toString());
            row.getCell(11).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
        }
    }

    private void setarDeflatoresExcelPadraoEB1(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet("Tipo Projeto");
        int rowNum = 2;
        List<FatorAjuste> fatorAjusteList = analise.getManual().getFatoresAjuste().stream().collect(Collectors.toList());
        for(int i = 0; i < fatorAjusteList.size(); i++) {
            FatorAjuste fatorAjuste = fatorAjusteList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(9).setCellValue(fatorAjuste.getNome());
            row.getCell(10)
                .setCellValue(fatorAjuste.getTipoAjuste().equals(TipoFatorAjuste.PERCENTUAL) ? fatorAjuste.getFator().doubleValue()/100 : fatorAjuste.getFator().doubleValue());
            row.getCell(11)
                .setCellValue(fatorAjuste.getTipoAjuste().equals(TipoFatorAjuste.PERCENTUAL) ? "%": "Q");
        }
    }

    private void setarResumoExcelPadraoEB1(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(RESUMO);
        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();
        if(analise.getNumeroOs() != null){
            excelSheet.getRow(3).getCell(1).setCellValue(analise.getNumeroOs());
        }else{
            excelSheet.getRow(3).getCell(1).setCellValue(analise.getIdentificadorAnalise());
        }
        if(analise.getSistema() != null){
            excelSheet.getRow(4).getCell(5).setCellValue(analise.getSistema().getNome());
        }
        switch(analise.getMetodoContagem()){
            case DETALHADA:
                excelSheet.getRow(4).getCell(1).setCellValue(METODO_DETALHADO);
                break;
            case ESTIMADA:
                excelSheet.getRow(4).getCell(1).setCellValue(METODO_ESTIMATIVA);
                break;
            case INDICATIVA:
                excelSheet.getRow(4).getCell(1).setCellValue(METODO_INDICATIVA);
                break;
        }
        excelSheet.getRow(12).getCell(0).setCellValue(analise.getEscopo());
        excelSheet.getRow(5).getCell(5).setCellValue(analise.getDataCriacaoOrdemServico());
        excelSheet.getRow(5).getCell(1).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());

        this.setarEsforcoFaseExcelPadraoBasis(excelSheet, analise, excelFile, evaluator);
    }

    //ANAC

    private ByteArrayOutputStream modeloPadraoANAC(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo3-anac.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);
        this.setarResumoExcelPadraoANAC(excelFile, analise);
        this.setarFuncoesPadraoANAC(excelFile, funcaoDadosList, funcaoTransacaoList);
        this.setarPFPorFuncionalidade(excelFile, funcaoDadosList, funcaoTransacaoList);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }

    private void setarFuncoesPadraoANAC(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) {
        XSSFSheet excelSheet = excelFile.getSheet("Funções");
        int rowNum = 2;
        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            String nome = funcaoDados.getFuncionalidade().getNome() + " - " + funcaoDados.getName();
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(1).setCellValue(nome);
            row.getCell(2).setCellValue(funcaoDados.getTipo().toString());
            row.getCell(3).setCellValue(funcaoDados.getFatorAjuste().getSigla() == null ? "" : funcaoDados.getFatorAjuste().getSigla());
            row.getCell(4).setCellValue(this.getTotalDer(funcaoDados.getDers()));
            String ders = funcaoDados.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(5).setCellValue(ders);
            row.getCell(6).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()));
            String rlrs = funcaoDados.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(7).setCellValue(rlrs);
            row.getCell(8).setCellValue(funcaoDados.getQuantidade() != null ? funcaoDados.getQuantidade() : Integer.valueOf(0));
            row.getCell(17).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
        }
        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            String nome = funcaoTransacao.getFuncionalidade().getNome() + " - " + funcaoTransacao.getName();
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(1).setCellValue(nome);
            row.getCell(2).setCellValue(funcaoTransacao.getTipo().toString());
            row.getCell(3).setCellValue(funcaoTransacao.getFatorAjuste().getSigla() == null ? "" : funcaoTransacao.getFatorAjuste().getSigla());
            row.getCell(4).setCellValue(this.getTotalDer(funcaoTransacao.getDers()));
            String ders = funcaoTransacao.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(5).setCellValue(ders);
            row.getCell(6).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()));
            String alrs = funcaoTransacao.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(7).setCellValue(alrs);
            row.getCell(8).setCellValue(funcaoTransacao.getQuantidade() != null ? funcaoTransacao.getQuantidade() : Integer.valueOf(0));
            row.getCell(17).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation()  != null ? funcaoTransacao.getSustantation() : "").text());
        }
    }


    private void setarResumoExcelPadraoANAC(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet(RESUMO);
        if(analise.getSistema() != null){
            excelSheet.getRow(8).getCell(3).setCellValue(analise.getSistema().getNome());
        }
        excelSheet.getRow(11).getCell(3)
            .setCellValue(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA) ? "Contagem Estimada" : "Contagem Detalhada");
        excelSheet.getRow(41).getCell(1).setCellValue(analise.getPropositoContagem());
        excelSheet.getRow(43).getCell(1).setCellValue(analise.getEscopo());
        excelSheet.getRow(45).getCell(1).setCellValue(analise.getFronteiras());
        excelSheet.getRow(10).getCell(3).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());

    }

    //BNDES

    private ByteArrayOutputStream modeloPadraoBNDES(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo2-bndes.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);
        this.setarResumoExcelPadraoBNDES(excelFile, analise);
        this.setarFuncoesPadraoBNDES(excelFile, funcaoDadosList, funcaoTransacaoList);
        if(analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
            this.setarFuncoesDetalhadaExcelPadraoBNDES(excelFile, funcaoDadosList, funcaoTransacaoList);
        }
        this.setarPFPorFuncionalidade(excelFile, funcaoDadosList, funcaoTransacaoList);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }

    private void setarFuncoesDetalhadaExcelPadraoBNDES(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) {
        this.setarFuncoesDadosExcelPadraoBNDES(excelFile, funcaoDadosList);
        this.setarFuncoesTransacaoExcelPadraoBNDES(excelFile, funcaoTransacaoList);
    }

    private void setarFuncoesDadosExcelPadraoBNDES(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList) {
        XSSFSheet excelSheet = excelFile.getSheet("Funções de Dados - Detalhe");
        int rowNum = 6;
        for(int i = 0; i < funcaoDadosList.size(); i++){
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            String nome = funcaoDados.getFuncionalidade().getNome() + " - " + funcaoDados.getName();
            row.getCell(0).setCellValue(nome);
            row.getCell(3).setCellValue(this.getTotalDer(funcaoDados.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoDados.getDers())) : "");
            String ders = funcaoDados.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(4).setCellValue(ders);
            row.getCell(5).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()) != 0 ? String.valueOf(this.getTotalRlr(funcaoDados.getRlrs())) : "");
            String rlrs = funcaoDados.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(6).setCellValue(rlrs);
        }
    }

    private void setarFuncoesTransacaoExcelPadraoBNDES(XSSFWorkbook excelFile, List<FuncaoTransacao> funcaoTransacaoList) {
        XSSFSheet excelSheet = excelFile.getSheet("Funções de Transação - Detalhe");
        int rowNum = 6;
        for(int i = 0; i < funcaoTransacaoList.size(); i++){
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            XSSFRow row = excelSheet.getRow(rowNum++);
            String nome = funcaoTransacao.getFuncionalidade().getNome() + " - " + funcaoTransacao.getName();
            row.getCell(0).setCellValue(nome);
            row.getCell(3).setCellValue(this.getTotalDer(funcaoTransacao.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoTransacao.getDers())) : "");
            String ders = funcaoTransacao.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(4).setCellValue(ders);
            row.getCell(5).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()) != 0 ? String.valueOf(this.getTotalAlr(funcaoTransacao.getAlrs())) : "");
            String alrs = funcaoTransacao.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(6).setCellValue(alrs);
        }
    }

    private void setarFuncoesPadraoBNDES(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) {
        XSSFSheet excelSheet = excelFile.getSheet("Planilha");
        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();
        int rowNum = 6;
        this.setarFuncoesDadosEstimadaExcelPadraoBNDES(funcaoDadosList, excelSheet, evaluator, rowNum);
        rowNum += funcaoDadosList.size()+1;
        this.setarFuncoesTransacaoEstimadaExcelPadraoBNDES(funcaoTransacaoList, excelSheet, evaluator, rowNum);
    }

    private void setarFuncoesTransacaoEstimadaExcelPadraoBNDES(List<FuncaoTransacao> funcaoTransacaoList, XSSFSheet excelSheet, FormulaEvaluator evaluator, int rowNum) {
        for(int i = 0; i < funcaoTransacaoList.size(); i++){
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            String nome = funcaoTransacao.getFuncionalidade().getNome() + " - " + funcaoTransacao.getName();
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(nome);
            row.getCell(9).setCellValue(this.getImpactoFromFatorAjuste(funcaoTransacao.getFatorAjuste()));
            row.getCell(8).setCellValue(funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM) ? "" : funcaoTransacao.getTipo().toString());
            evaluator.evaluateFormulaCell(row.getCell(8));
            row.getCell(10).setCellValue(this.getTotalDer(funcaoTransacao.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoTransacao.getDers())) : "");
            row.getCell(11).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()) != 0 ? String.valueOf(this.getTotalAlr(funcaoTransacao.getAlrs())) : "");
            row.getCell(23).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
        }
    }

    private void setarFuncoesDadosEstimadaExcelPadraoBNDES(List<FuncaoDados> funcaoDadosList, XSSFSheet excelSheet, FormulaEvaluator evaluator, int rowNum) {
        for(int i = 0; i < funcaoDadosList.size(); i++){
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            String nome = funcaoDados.getFuncionalidade().getNome() + " - " + funcaoDados.getName();
            XSSFRow row = excelSheet.getRow(rowNum++);
            row.getCell(0).setCellValue(nome);
            row.getCell(8).setCellValue(funcaoDados.getTipo().equals(TipoFuncaoDados.INM) ? "" : funcaoDados.getTipo().toString());
            evaluator.evaluateFormulaCell(row.getCell(8));
            row.getCell(9).setCellValue(this.getImpactoFromFatorAjuste(funcaoDados.getFatorAjuste()));
            row.getCell(10).setCellValue(this.getTotalDer(funcaoDados.getDers()) != 0 ? String.valueOf(this.getTotalDer(funcaoDados.getDers())) : "");
            row.getCell(11).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()) != 0 ? String.valueOf(this.getTotalRlr(funcaoDados.getRlrs())) : "");
            row.getCell(23).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
        }
    }

    private String getImpactoFromFatorAjuste(FatorAjuste fatorAjuste){
        switch(fatorAjuste.getFator().intValue()){
            case 100:
                return "I";
            case 50:
                return "A";
            case 40:
                return "E";
            case 15:
                return "T";
            default:
                return "C";
        }
    }

    private void setarResumoExcelPadraoBNDES(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet excelSheet = excelFile.getSheet("Identificação");
        if(analise.getSistema() != null){
            excelSheet.getRow(3).getCell(5).setCellValue(analise.getSistema().getNome());
        }
        excelSheet.getRow(6).getCell(5).setCellValue(analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto());
        excelSheet.getRow(6).getCell(20).setCellValue(analise.getDataCriacaoOrdemServico());
        excelSheet.getRow(22).getCell(0).setCellValue(analise.getEscopo());
        excelSheet.getRow(11).getCell(0).setCellValue(analise.getPropositoContagem());
    }
    //Padrão BASIS

    private ByteArrayOutputStream modeloPadraoBasis(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo1-basis.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);
        this.setarPFPorFuncionalidade(excelFile, funcaoDadosList, funcaoTransacaoList);
        this.setarDeflatoresExcelPadraoBasis(excelFile, analise);
        String nomeElaborador = analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
            analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto();
        this.setarResumoExcelPadraoBasis(excelFile, analise, nomeElaborador);
        if(analise.getMetodoContagem().equals(MetodoContagem.INDICATIVA)){
            this.setarFuncoesIndicativaExcelPadraoBasis(excelFile, funcaoDadosList, analise, nomeElaborador);
        }
        else{
            this.setarFuncoesINMExcelPadraoBasis(excelFile, funcaoTransacaoList, analise, nomeElaborador, false);
            if(analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
                this.setarFuncoesDetalhadaExcelPadraoBasis(excelFile, funcaoDadosList, funcaoTransacaoList, analise, nomeElaborador, false);
            }
            else if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
                this.setarFuncoesEstimadaExcelPadraoBasis(excelFile, funcaoDadosList, funcaoTransacaoList, analise, nomeElaborador);
            }
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }


    private void setarDeflatoresExcelPadraoBasis(XSSFWorkbook excelFile, Analise analise) {
        XSSFSheet deflatorSheet = excelFile.getSheet("Tipo Projeto");
        int rownum = 2;
        int rowNumUnitario = 2;
        List<FatorAjuste> fatorAjusteList = analise.getManual().getFatoresAjuste().stream().collect(Collectors.toList());
        for(int i = 0; i < fatorAjusteList.size(); i++){
            FatorAjuste fatorAjuste = fatorAjusteList.get(i);
            if(fatorAjuste.getTipoAjuste().equals(TipoFatorAjuste.PERCENTUAL)){
                XSSFRow row = deflatorSheet.getRow(rownum++);

                row.getCell(0).setCellValue(fatorAjuste.getNome());
                row.getCell(1).setCellValue(fatorAjuste.getFator().doubleValue()/100);
            }else if(fatorAjuste.getTipoAjuste().equals(TipoFatorAjuste.UNITARIO)){
                XSSFRow row = deflatorSheet.getRow(rowNumUnitario++);

                if (row.getCell(9) != null) {
                    row.getCell(9).setCellValue(fatorAjuste.getNome());
                }
                if (row.getCell(10) != null) {
                    row.getCell(10).setCellValue(fatorAjuste.getFator().doubleValue());
                }
                if (row.getCell(13) != null) {
                    row.getCell(13).setCellValue(fatorAjuste.getDescricao());
                }
            }
        }
    }


    private void setarResumoExcelPadraoBasis(XSSFWorkbook excelFile, Analise analise, String nomeElaborador){
        XSSFSheet excelSheet = excelFile.getSheet(RESUMO);
        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();
        if(analise.getNumeroOs() != null){
            excelSheet.getRow(3).getCell(1).setCellValue(analise.getNumeroOs());
        }
        excelSheet.getRow(4).getCell(5).setCellValue(analise.getSistema().getNome());
        excelSheet.getRow(5).getCell(5).setCellValue(analise.getDataCriacaoOrdemServico());
        excelSheet.getRow(5).getCell(1).setCellValue(nomeElaborador);
        excelSheet.getRow(12).getCell(0).setCellValue(analise.getEscopo());
        switch(analise.getMetodoContagem()){
            case ESTIMADA:
                excelSheet.getRow(4).getCell(1).setCellValue(METODO_ESTIMATIVA);
                break;
            case DETALHADA:
                excelSheet.getRow(4).getCell(1).setCellValue(METODO_DETALHADO);
                break;
            case INDICATIVA:
                excelSheet.getRow(4).getCell(1).setCellValue(METODO_INDICATIVA);
                break;
        }
        evaluator.evaluateFormulaCell(excelSheet.getRow(4).getCell(1));
        for(int i = 15; i < 23; i++){
            evaluator.evaluate(excelSheet.getRow(i).getCell(2));
        }
        this.setarEsforcoFaseExcelPadraoBasis(excelSheet, analise, excelFile, evaluator);
    }


    private void setarEsforcoFaseExcelPadraoBasis(XSSFSheet excelSheet, Analise analise, XSSFWorkbook excelFile, FormulaEvaluator evaluator) {
        int celulaEsforco = 17;
        for (int i = 0; i < 6; i++){
            excelSheet.getRow(celulaEsforco).getCell(4).setCellValue("");
            excelSheet.getRow(celulaEsforco++).getCell(6).setCellValue("");
        }
        celulaEsforco = 17;
        for (int i = 0; i < analise.getEsforcoFases().size(); i++){
            EsforcoFase fase = analise.getEsforcoFases().stream().collect(Collectors.toList()).get(i);
            excelSheet.getRow(celulaEsforco).getCell(4).setCellValue(fase.getFase().getNome());
            excelSheet.getRow(celulaEsforco).getCell(6).getCellStyle().setDataFormat(excelFile.createDataFormat().getFormat("00%"));
            excelSheet.getRow(celulaEsforco++).getCell(6).setCellValue(fase.getEsforco().doubleValue()/100);
        }
        evaluator.evaluateFormulaCell(excelSheet.getRow(23).getCell(6));
    }

    private void setarFuncoesDetalhadaExcelPadraoBasis(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise, String nomeElaborador, boolean isDivergence) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);

        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();

        excelSheet.getRow(2).getCell(13).setCellValue(nomeElaborador);
        excelSheet.getRow(3).getCell(13).setCellValue(analise.getDataCriacaoOrdemServico());

        int rowNumero = 9;
        int idRow = 1;

        for (int i = 0; i < funcaoDadosList.size(); i++){
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            XSSFRow row = excelSheet.getRow(rowNumero++);

            row.getCell(5).setCellValue(funcaoDados.getName());
            row.getCell(6).setCellValue(funcaoDados.getTipo().toString());
            row.getCell(7).setCellValue(this.getTotalDer(funcaoDados.getDers()));row.getCell(0).setCellValue(idRow++);
            row.getCell(1).setCellValue(funcaoDados.getFatorAjuste().getNome());
            evaluator.evaluateFormulaCell(row.getCell(2));row.getCell(3).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(4).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(9).setCellValue(this.getTotalRlr(funcaoDados.getRlrs()));
            String rlrs = funcaoDados.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(10).setCellValue(rlrs);
            String ders = funcaoDados.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(8).setCellValue(ders);
            row.getCell(17).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
            evaluator.evaluateFormulaCell(row.getCell(16));
            if(isDivergence == true){
                row.getCell(19).setCellValue(this.pegarValorValidacao(funcaoDados.getStatusFuncao()));
            }
        }


        for (int i = 0; i < funcaoTransacaoList.size(); i++){
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            if(!funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)){
                XSSFRow row = excelSheet.getRow(rowNumero++);
                row.getCell(6).setCellValue(funcaoTransacao.getTipo().toString());
                row.getCell(7).setCellValue(this.getTotalDer(funcaoTransacao.getDers()));
                row.getCell(3).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
                row.getCell(4).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
                row.getCell(5).setCellValue(funcaoTransacao.getName());
                String ders = funcaoTransacao.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                row.getCell(8).setCellValue(ders);
                String rlrs = funcaoTransacao.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                row.getCell(10).setCellValue(rlrs);
                row.getCell(9).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()));
                evaluator.evaluateFormulaCell(row.getCell(16));
                row.getCell(0).setCellValue(idRow++);
                row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
                row.getCell(17).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
                evaluator.evaluateFormulaCell(row.getCell(2));
                if(isDivergence == true){
                    row.getCell(19).setCellValue(this.pegarValorValidacao(funcaoTransacao.getStatusFuncao()));
                }
            }
        }
        evaluator.evaluateFormulaCell(excelSheet.getRow(4).getCell(3));
    }

    private void setarFuncoesEstimadaExcelPadraoBasis(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise, String nomeElaborador) {
        XSSFSheet excelSheetEstimada = excelFile.getSheet(ESTIMATIVA);

        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();

        excelSheetEstimada.getRow(2).getCell(7).setCellValue(nomeElaborador);
        excelSheetEstimada.getRow(3).getCell(7).setCellValue(analise.getDataCriacaoOrdemServico());

        int rownum = 10;
        int idRow = 1;

        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            XSSFRow row = excelSheetEstimada.getRow(rownum++);
            row.getCell(0).setCellValue(idRow++);
            row.getCell(1).setCellValue(funcaoDados.getFatorAjuste().getNome());
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(4).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(5).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(6).setCellValue(funcaoDados.getName());
            row.getCell(7).setCellValue(funcaoDados.getTipo().toString());
            row.getCell(9).setCellValue(Jsoup.parse(funcaoDados.getSustantation() != null ? funcaoDados.getSustantation() : "").text());
            evaluator.evaluateFormulaCell(row.getCell(8));
        }

        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
            if(!funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)){
                XSSFRow row = excelSheetEstimada.getRow(rownum++);
                row.getCell(0).setCellValue(idRow++);
                row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
                evaluator.evaluateFormulaCell(row.getCell(2));
                row.getCell(4).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
                row.getCell(5).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
                row.getCell(6).setCellValue(funcaoTransacao.getName());
                row.getCell(9).setCellValue(Jsoup.parse(funcaoTransacao.getSustantation() != null ? funcaoTransacao.getSustantation() : "").text());
                row.getCell(7).setCellValue(funcaoTransacao.getTipo().toString());
                evaluator.evaluateFormulaCell(row.getCell(8));
            }
        }
        evaluator.evaluateFormulaCell(excelSheetEstimada.getRow(4).getCell(2));
    }


    private void setarFuncoesIndicativaExcelPadraoBasis(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, Analise analise, String nomeElaborador) {
        XSSFSheet excelSheet = excelFile.getSheet("AFP - Indicativa");
        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();

        excelSheet.getRow(2).getCell(6).setCellValue(nomeElaborador);
        excelSheet.getRow(3).getCell(6).setCellValue(analise.getDataCriacaoOrdemServico());

        int rownum = 9;
        int idRow = 1;
        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcaoDados = funcaoDadosList.get(i);
            XSSFRow row = excelSheet.getRow(rownum++);
            row.getCell(0).setCellValue(idRow++);
            row.getCell(2).setCellValue(funcaoDados.getFuncionalidade().getModulo().getNome());
            row.getCell(3).setCellValue(funcaoDados.getFuncionalidade().getNome());
            row.getCell(5).setCellValue(funcaoDados.getName());
            row.getCell(6).setCellValue(funcaoDados.getTipo().toString());
            evaluator.evaluateFormulaCell(row.getCell(7));
        }
        evaluator.evaluateFormulaCell(excelSheet.getRow(4).getCell(3));
    }

    private void setarFuncoesINMExcelPadraoBasis(XSSFWorkbook excelFile, List<FuncaoTransacao> funcaoTransacaoList, Analise analise, String nomeElaborador, boolean isDivergence) {
        XSSFSheet excelSheet = excelFile.getSheet(SHEET_INM);
        if(excelSheet != null){
            excelSheet.getRow(2).getCell(15).setCellValue(nomeElaborador);
            excelSheet.getRow(3).getCell(15).setCellValue(analise.getDataCriacaoOrdemServico());
            FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();
            int rownum = 10;
            int idRow = 1;

            for (int i = 0; i < funcaoTransacaoList.size(); i++) {
                FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
                if(funcaoTransacao.getTipo().equals(TipoFuncaoTransacao.INM)){
                    XSSFRow row = excelSheet.getRow(rownum++);
                    row.getCell(0).setCellValue(idRow++);
                    row.getCell(1).setCellValue(funcaoTransacao.getFatorAjuste().getNome());
                    evaluator.evaluateFormulaCell(row.getCell(2));
                    row.getCell(5).setCellValue(funcaoTransacao.getFuncionalidade().getModulo().getNome());
                    row.getCell(6).setCellValue(funcaoTransacao.getFuncionalidade().getNome());
                    row.getCell(7).setCellValue(funcaoTransacao.getName());
                    row.getCell(9).setCellValue(funcaoTransacao.getQuantidade() != null ? funcaoTransacao.getQuantidade() : Integer.valueOf(0));
                    row.getCell(10).setCellValue(this.getTotalDer(funcaoTransacao.getDers()));
                    row.getCell(11).setCellValue(this.getTotalAlr(funcaoTransacao.getAlrs()));
                    row.getCell(12).setCellValue(TIPO_INM);
                    row.getCell(19).setCellValue(this.getFundamentacao(funcaoTransacao));
                    evaluator.evaluateFormulaCell(row.getCell(18));
                    if(isDivergence == true){
                        row.getCell(21).setCellValue(this.pegarValorValidacao(funcaoTransacao.getStatusFuncao()));
                    }
                }
            }
            evaluator.evaluateFormulaCell(excelSheet.getRow(4).getCell(3));
        }
    }

    private String pegarValorValidacao(StatusFuncao statusFuncao) {
        if(statusFuncao != null){
            if(statusFuncao.equals(StatusFuncao.VALIDADO)){
                return "OK";
            }else if(statusFuncao.equals(StatusFuncao.DIVERGENTE)){
                return DIVERGENTE;
            }else{
                return "";
            }
        }

        return "";
    }

    private void setarPFPorFuncionalidade(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) {
        XSSFSheet sheet = excelFile.getSheet(PF_POR_FUNCIONALIDADE);
        Map<String, Double> pfPorFunc = new HashMap<>();
        if(sheet != null){
            for (int i = 0; i < funcaoDadosList.size(); i++) {
                FuncaoDados funcaoDados = funcaoDadosList.get(i);
                if(!pfPorFunc.containsKey(funcaoDados.getFuncionalidade().getNome())){
                    pfPorFunc.put(funcaoDados.getFuncionalidade().getNome(), funcaoDados.getPf().doubleValue());
                }else{
                    pfPorFunc.put(funcaoDados.getFuncionalidade().getNome(), pfPorFunc.get(funcaoDados.getFuncionalidade().getNome()) + funcaoDados.getPf().doubleValue());
                }
            }
            for (int i = 0; i < funcaoTransacaoList.size(); i++) {
                FuncaoTransacao funcaoTransacao = funcaoTransacaoList.get(i);
                if(!pfPorFunc.containsKey(funcaoTransacao.getFuncionalidade().getNome())){
                    pfPorFunc.put(funcaoTransacao.getFuncionalidade().getNome(), funcaoTransacao.getPf().doubleValue());
                }else{
                    pfPorFunc.put(funcaoTransacao.getFuncionalidade().getNome(), pfPorFunc.get(funcaoTransacao.getFuncionalidade().getNome()) + funcaoTransacao.getPf().doubleValue());
                }
            }
            int rowNum = 4;
            int num = 1;
            for (Map.Entry<String, Double> entry : pfPorFunc.entrySet()) {
                XSSFRow row = sheet.getRow(rowNum++);
                row.getCell(0).setCellValue(num++);
                row.getCell(1).setCellValue(entry.getKey());
                row.getCell(2).setCellValue(entry.getValue());
            }
        }
    }


    public ByteArrayOutputStream selecionarModeloDivergencia(Analise analise, Long modelo) throws IOException {
        if(modelo == 1){
            List<FuncaoDados> funcaoDadosList = analise.getFuncaoDados().stream().collect(Collectors.toList());
            List<FuncaoTransacao> funcaoTransacaoList = analise.getFuncaoTransacaos().stream().collect(Collectors.toList());
            return this.modeloPadraoBasisDivergencia(analise, funcaoDadosList, funcaoTransacaoList);
        }
        return new ByteArrayOutputStream();
    }

    private ByteArrayOutputStream modeloPadraoBasisDivergencia(Analise analise, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("reports/planilhas/modelo1-basis.xlsx");
        XSSFWorkbook excelFile = new XSSFWorkbook(stream);
        this.setarDeflatoresExcelPadraoBasis(excelFile, analise);

        Analise analisePrincipal = new Analise();
        String nomeElaborador;
        for (Analise analiseFor : analise.getAnalisesComparadas()) {
            if(analiseFor.getEquipeResponsavel().getNome().toLowerCase().contains(BASIS_MINUSCULO)){
                analisePrincipal = analiseFor;
            }
        }
        if(analisePrincipal.getId() != null){
            nomeElaborador = analisePrincipal.getEquipeResponsavel().getCfpsResponsavel() != null ?
                analisePrincipal.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analisePrincipal.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analisePrincipal.getEquipeResponsavel().getPreposto();
        }else{
            nomeElaborador = analise.getEquipeResponsavel().getCfpsResponsavel() != null ?
                analise.getEquipeResponsavel().getCfpsResponsavel().getFirstName() + " "+ analise.getEquipeResponsavel().getCfpsResponsavel().getLastName() : analise.getEquipeResponsavel().getPreposto();
        }

        this.setarResumoExcelPadraoBasis(excelFile, analisePrincipal, nomeElaborador);
        this.setarFuncoesDetalhadaExcelPadraoBasisDivergencia(excelFile, funcaoDadosList, funcaoTransacaoList, analisePrincipal, nomeElaborador);
        this.setarPFPorFuncionalidade(excelFile, funcaoDadosList, funcaoTransacaoList);
        this.setarFuncoesINMExcelPadraoBasisDivergencia(excelFile, funcaoTransacaoList, analisePrincipal, nomeElaborador);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        excelFile.write(outputStream);
        return outputStream;
    }

    private boolean testarFuncaoTransacaoDivergenciaINM(FuncaoTransacao funcaoPrimariaINM, FuncaoTransacao funcaoSecundariaINM) {
        if((funcaoSecundariaINM != null && funcaoPrimariaINM.getName() != null
            && funcaoPrimariaINM.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)
            && funcaoSecundariaINM.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)) ||
            funcaoSecundariaINM == null && funcaoPrimariaINM.getName() != null
                && funcaoPrimariaINM.getStatusFuncao().equals(StatusFuncao.EXCLUIDO) ||
            funcaoSecundariaINM != null && funcaoPrimariaINM.getName() == null
                && funcaoSecundariaINM.getStatusFuncao().equals(StatusFuncao.EXCLUIDO) ||
            funcaoPrimariaINM.getName() != null && !funcaoPrimariaINM.getTipo().equals(TipoFuncaoTransacao.INM) ||
            funcaoSecundariaINM != null && !funcaoSecundariaINM.getTipo().equals(TipoFuncaoTransacao.INM)){
            return true;
        }
        return false;

    }


    private void setarFuncoesINMExcelPadraoBasisDivergencia(XSSFWorkbook excelFile, List<FuncaoTransacao> funcaoTransacaoList, Analise analise, String nomeElaborador) {
        XSSFSheet excelSheet = excelFile.getSheet(SHEET_INM);
        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();

        excelSheet.getRow(2).getCell(15).setCellValue(nomeElaborador);
        excelSheet.getRow(3).getCell(15).setCellValue(analise.getDataCriacaoOrdemServico());

        int rowNumero = 10;
        int idRow = 1;

        Map<FuncaoTransacao, FuncaoTransacao> funcaoTransacao = new LinkedHashMap<>();
        this.carregarFuncoesTransacaoDivergencia(funcaoTransacaoList, funcaoTransacao);
        this.setarINMExcelDivergenciaPadraoBasis(funcaoTransacao, excelSheet, rowNumero, idRow, evaluator);
        evaluator.evaluateFormulaCell(excelSheet.getRow(4).getCell(3));
    }

    private void setarINMExcelDivergenciaPadraoBasis(Map<FuncaoTransacao, FuncaoTransacao> funcaoTransacao, XSSFSheet excelSheet, int rowNumero, int idRow, FormulaEvaluator evaluator) {
        for (Map.Entry<FuncaoTransacao, FuncaoTransacao> funcao : funcaoTransacao.entrySet()) {
            boolean canContinue = true;
            FuncaoTransacao funcaoPrimaria = funcao.getKey();
            FuncaoTransacao funcaoSecundaria = funcao.getValue();
            if(this.testarFuncaoTransacaoDivergenciaINM(funcaoPrimaria, funcaoSecundaria) == true) {
                canContinue = false;
            }
            if(canContinue){
                XSSFRow row = excelSheet.getRow(rowNumero++);
                this.setarFuncoesINMTransacaoExcelDivergencia(funcaoPrimaria, funcaoSecundaria, row, idRow++, evaluator);
            }
        }
    }


    private void setarFuncoesINMTransacaoExcelDivergencia(FuncaoTransacao funcaoPrimaria, FuncaoTransacao funcaoSecundaria, XSSFRow row, int idRow, FormulaEvaluator evaluator) {
        if(funcaoPrimaria.getName() != null){
            row.getCell(5).setCellValue(funcaoPrimaria.getFuncionalidade().getModulo().getNome());
            row.getCell(6).setCellValue(funcaoPrimaria.getFuncionalidade().getNome());
            row.getCell(1).setCellValue(funcaoPrimaria.getFatorAjuste().getNome());
            row.getCell(7).setCellValue(funcaoPrimaria.getName());
            row.getCell(0).setCellValue(idRow);
            row.getCell(9).setCellValue(this.getQuantidadeFuncao(funcaoPrimaria));
            row.getCell(10).setCellValue(this.getTotalDer(funcaoPrimaria.getDers()));
            row.getCell(11).setCellValue(this.getTotalAlr(funcaoPrimaria.getAlrs()));
            row.getCell(12).setCellValue(TIPO_INM);
            row.getCell(19).setCellValue(this.getFundamentacao(funcaoPrimaria));
            row.getCell(21).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoPrimaria, funcaoSecundaria));
            evaluator.evaluateFormulaCell(row.getCell(16));
            evaluator.evaluateFormulaCell(row.getCell(18));
        }else{
            if(funcaoSecundaria != null){
                if(funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.VALIDADO)){
                    row.getCell(1).setCellValue(funcaoSecundaria.getFatorAjuste().getNome());
                    row.getCell(5).setCellValue(funcaoSecundaria.getFuncionalidade().getModulo().getNome());
                    row.getCell(6).setCellValue(funcaoSecundaria.getFuncionalidade().getNome());
                    row.getCell(7).setCellValue(funcaoSecundaria.getName());
                    row.getCell(0).setCellValue(idRow);
                    row.getCell(9).setCellValue(this.getQuantidadeFuncao(funcaoSecundaria));
                    row.getCell(10).setCellValue(this.getTotalDer(funcaoSecundaria.getDers()));
                    row.getCell(11).setCellValue(this.getTotalAlr(funcaoSecundaria.getAlrs()));
                    row.getCell(12).setCellValue(TIPO_INM);
                    row.getCell(19).setCellValue(this.getFundamentacao(funcaoSecundaria));
                    row.getCell(21).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoSecundaria, funcaoSecundaria));
                    evaluator.evaluateFormulaCell(row.getCell(16));
                    evaluator.evaluateFormulaCell(row.getCell(18));
                }else{
                    row.getCell(6).setCellValue(funcaoSecundaria.getFuncionalidade().getNome());
                    row.getCell(5).setCellValue(funcaoSecundaria.getFuncionalidade().getModulo().getNome());
                    row.getCell(7).setCellValue(funcaoSecundaria.getName());
                }
            }
        }
        if(funcaoSecundaria != null){
            row.getCell(21).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoPrimaria, funcaoSecundaria));
            row.getCell(22).setCellValue(funcaoSecundaria.getFatorAjuste().getNome());
            row.getCell(25).setCellValue(this.getQuantidadeFuncao(funcaoSecundaria));
            row.getCell(26).setCellValue(this.getTotalDer(funcaoSecundaria.getDers()));
            row.getCell(27).setCellValue(this.getTotalAlr(funcaoSecundaria.getAlrs()));
            row.getCell(28).setCellValue(TIPO_INM);
            row.getCell(35).setCellValue(this.getFundamentacao(funcaoSecundaria));
            evaluator.evaluateFormulaCell(row.getCell(31));
            evaluator.evaluateFormulaCell(row.getCell(34));
        }
    }

    private String getFundamentacao(FuncaoTransacao funcao) {
        return Jsoup.parse(funcao.getSustantation() != null ? funcao.getSustantation() : "").text();
    }

    private Integer getQuantidadeFuncao(FuncaoTransacao funcao) {
        return funcao.getQuantidade() != null ? funcao.getQuantidade() : Integer.valueOf(0);
    }


    private void setarFuncoesDetalhadaExcelPadraoBasisDivergencia(XSSFWorkbook excelFile, List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Analise analise, String nomeElaborador) {
        XSSFSheet excelSheet = excelFile.getSheet(DETALHADA);

        FormulaEvaluator evaluator = excelFile.getCreationHelper().createFormulaEvaluator();

        excelSheet.getRow(2).getCell(13).setCellValue(nomeElaborador);
        excelSheet.getRow(3).getCell(13).setCellValue(analise.getDataCriacaoOrdemServico());

        int rowNumero = 9;
        int idRow = 1;

        Map<FuncaoDados, FuncaoDados> funcaoDados = new LinkedHashMap<>();
        Map<FuncaoTransacao, FuncaoTransacao> funcaoTransacao = new LinkedHashMap<>();
        this.carregarFuncoesDivergencia(funcaoDadosList, funcaoTransacaoList, funcaoDados, funcaoTransacao);
        idRow = this.setarFuncoesDadosExcelDivergenciaPadraoBasis(funcaoDados, excelSheet, rowNumero, idRow, evaluator);
        rowNumero += idRow;
        this.setarFuncoesTransacaoExcelDivergenciaPadraoBasis(funcaoTransacao, excelSheet, --rowNumero, idRow, evaluator);
        evaluator.evaluateFormulaCell(excelSheet.getRow(4).getCell(3));
    }

    private void setarFuncoesTransacaoExcelDivergenciaPadraoBasis(Map<FuncaoTransacao, FuncaoTransacao> funcaoTransacao, XSSFSheet excelSheet, int rowNumero, int idRow, FormulaEvaluator evaluator) {
        for (Map.Entry<FuncaoTransacao, FuncaoTransacao> funcao : funcaoTransacao.entrySet()) {
            boolean canContinue = true;
            FuncaoTransacao funcaoPrimaria = funcao.getKey();
            FuncaoTransacao funcaoSecundaria = funcao.getValue();
            if(this.testarFuncaoTransacaoDivergencia(funcaoPrimaria, funcaoSecundaria) == true) {
                canContinue = false;
            }

            if(canContinue){
                XSSFRow row = excelSheet.getRow(rowNumero++);
                this.setarFuncoesTransacaoExcelDivergencia(funcaoPrimaria, funcaoSecundaria, row, idRow++, evaluator);
            }
        }
    }

    private void setarFuncoesTransacaoExcelDivergencia(FuncaoTransacao funcaoPrimaria, FuncaoTransacao funcaoSecundaria, XSSFRow row, int idRow, FormulaEvaluator evaluator) {
        row.getCell(0).setCellValue(idRow);
        if(funcaoPrimaria.getName() != null){
            row.getCell(5).setCellValue(funcaoPrimaria.getName());
            row.getCell(6).setCellValue(funcaoPrimaria.getTipo().toString());
            row.getCell(7).setCellValue(this.getTotalDer(funcaoPrimaria.getDers()));
            row.getCell(1).setCellValue(funcaoPrimaria.getFatorAjuste().getNome());
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(3).setCellValue(funcaoPrimaria.getFuncionalidade().getModulo().getNome());
            row.getCell(4).setCellValue(funcaoPrimaria.getFuncionalidade().getNome());
            row.getCell(9).setCellValue(this.getTotalAlr(funcaoPrimaria.getAlrs()));
            String alrs = funcaoPrimaria.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(10).setCellValue(alrs);
            String ders = funcaoPrimaria.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(8).setCellValue(ders);
            row.getCell(17).setCellValue(Jsoup.parse(funcaoPrimaria.getSustantation() != null ? funcaoPrimaria.getSustantation() : "").text());
            evaluator.evaluateFormulaCell(row.getCell(16));
            row.getCell(19).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoPrimaria, funcaoSecundaria));
            row.getCell(35).setCellValue(funcaoPrimaria.getLstDivergenceComments().stream().map(item -> item.getComment()).collect(Collectors.joining(", ")));
        }else{
            if(funcaoSecundaria != null){
                if(funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.VALIDADO)){
                    row.getCell(5).setCellValue(funcaoSecundaria.getName());
                    row.getCell(6).setCellValue(funcaoSecundaria.getTipo().toString());
                    row.getCell(7).setCellValue(this.getTotalDer(funcaoSecundaria.getDers()));
                    row.getCell(1).setCellValue(funcaoSecundaria.getFatorAjuste().getNome());
                    evaluator.evaluateFormulaCell(row.getCell(2));
                    row.getCell(3).setCellValue(funcaoSecundaria.getFuncionalidade().getModulo().getNome());
                    row.getCell(4).setCellValue(funcaoSecundaria.getFuncionalidade().getNome());
                    row.getCell(9).setCellValue(this.getTotalAlr(funcaoSecundaria.getAlrs()));
                    String alrs = funcaoSecundaria.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                    row.getCell(10).setCellValue(alrs);
                    String ders = funcaoSecundaria.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                    row.getCell(8).setCellValue(ders);
                }else{
                    row.getCell(4).setCellValue(funcaoSecundaria.getFuncionalidade().getNome());
                    row.getCell(3).setCellValue(funcaoSecundaria.getFuncionalidade().getModulo().getNome());
                    row.getCell(5).setCellValue(funcaoSecundaria.getName());
                }
            }
        }
        if(funcaoSecundaria != null){
            row.getCell(19).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoPrimaria, funcaoSecundaria));
            row.getCell(20).setCellValue(funcaoSecundaria.getFatorAjuste().getNome());
            row.getCell(22).setCellValue(funcaoSecundaria.getTipo().toString());
            String dersSecundaria = funcaoSecundaria.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(23).setCellValue(this.getTotalDer(funcaoSecundaria.getDers()));
            row.getCell(24).setCellValue(dersSecundaria);
            row.getCell(25).setCellValue(this.getTotalAlr(funcaoSecundaria.getAlrs()));
            String alrsSecundaria = funcaoSecundaria.getAlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(26).setCellValue(alrsSecundaria);
            row.getCell(33).setCellValue(funcaoSecundaria.getLstDivergenceComments().stream().map(item -> item.getComment()).collect(Collectors.joining(", ")));
        }
    }


    private boolean testarFuncaoTransacaoDivergencia(FuncaoTransacao funcaoPrimaria, FuncaoTransacao funcaoSecundaria) {
        if((funcaoSecundaria != null && funcaoPrimaria.getName() != null
            && funcaoPrimaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)
            && funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)) ||
            funcaoSecundaria == null && funcaoPrimaria.getName() != null
                && funcaoPrimaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO) ||
            funcaoSecundaria != null && funcaoPrimaria.getName() == null
                && funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO) ||
            funcaoPrimaria.getName() != null && funcaoPrimaria.getTipo().equals(TipoFuncaoTransacao.INM) ||
            funcaoSecundaria != null && funcaoSecundaria.getTipo().equals(TipoFuncaoTransacao.INM)){
            return true;
        }
        return false;
    }

    private int setarFuncoesDadosExcelDivergenciaPadraoBasis(Map<FuncaoDados, FuncaoDados> funcaoDados, XSSFSheet excelSheet, int rowNumero, int idRow, FormulaEvaluator evaluator) {
        for (Map.Entry<FuncaoDados, FuncaoDados> funcao : funcaoDados.entrySet()) {
            boolean canContinue = true;
            FuncaoDados funcaoDadosPrimaria = funcao.getKey();
            FuncaoDados funcaoDadosSecundaria = funcao.getValue();
            if(this.testarFuncaoDadoDivergencia(funcaoDadosPrimaria, funcaoDadosSecundaria) == true) {
                canContinue = false;
            }
            if(canContinue){
                XSSFRow row = excelSheet.getRow(rowNumero++);
                this.setarFuncoesDadosExcelDivergencia(funcaoDadosPrimaria, funcaoDadosSecundaria, row, idRow++, evaluator);
            }
        }
        return idRow;
    }

    private void setarFuncoesDadosExcelDivergencia(FuncaoDados funcaoDadosPrimaria, FuncaoDados funcaoDadosSecundaria, XSSFRow row, int idRow, FormulaEvaluator evaluator){
        row.getCell(0).setCellValue(idRow);
        if(funcaoDadosPrimaria.getName() != null){
            row.getCell(5).setCellValue(funcaoDadosPrimaria.getName());
            row.getCell(6).setCellValue(funcaoDadosPrimaria.getTipo().toString());
            row.getCell(7).setCellValue(this.getTotalDer(funcaoDadosPrimaria.getDers()));
            row.getCell(1).setCellValue(funcaoDadosPrimaria.getFatorAjuste().getNome());
            evaluator.evaluateFormulaCell(row.getCell(2));
            row.getCell(3).setCellValue(funcaoDadosPrimaria.getFuncionalidade().getModulo().getNome());
            row.getCell(4).setCellValue(funcaoDadosPrimaria.getFuncionalidade().getNome());
            row.getCell(9).setCellValue(this.getTotalRlr(funcaoDadosPrimaria.getRlrs()));
            String rlrs = funcaoDadosPrimaria.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(10).setCellValue(rlrs);
            String ders = funcaoDadosPrimaria.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(8).setCellValue(ders);
            row.getCell(17).setCellValue(Jsoup.parse(funcaoDadosPrimaria.getSustantation() != null ? funcaoDadosPrimaria.getSustantation() : "").text());
            evaluator.evaluateFormulaCell(row.getCell(16));
            row.getCell(19).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoDadosPrimaria, funcaoDadosSecundaria));
            row.getCell(35).setCellValue(funcaoDadosPrimaria.getLstDivergenceComments().stream().map(item -> item.getComment()).collect(Collectors.joining(", ")));
        }else{
            if(funcaoDadosSecundaria != null){
                if(funcaoDadosSecundaria.getStatusFuncao().equals(StatusFuncao.VALIDADO)){
                    row.getCell(5).setCellValue(funcaoDadosSecundaria.getName());
                    row.getCell(6).setCellValue(funcaoDadosSecundaria.getTipo().toString());
                    row.getCell(7).setCellValue(this.getTotalDer(funcaoDadosSecundaria.getDers()));
                    row.getCell(1).setCellValue(funcaoDadosSecundaria.getFatorAjuste().getNome());
                    evaluator.evaluateFormulaCell(row.getCell(2));
                    row.getCell(3).setCellValue(funcaoDadosSecundaria.getFuncionalidade().getModulo().getNome());
                    row.getCell(4).setCellValue(funcaoDadosSecundaria.getFuncionalidade().getNome());
                    row.getCell(9).setCellValue(this.getTotalRlr(funcaoDadosSecundaria.getRlrs()));
                    String alrs = funcaoDadosSecundaria.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                    row.getCell(10).setCellValue(alrs);
                    String ders = funcaoDadosSecundaria.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
                    row.getCell(8).setCellValue(ders);
                }else{
                    row.getCell(4).setCellValue(funcaoDadosSecundaria.getFuncionalidade().getNome());
                    row.getCell(3).setCellValue(funcaoDadosSecundaria.getFuncionalidade().getModulo().getNome());
                    row.getCell(5).setCellValue(funcaoDadosSecundaria.getName());
                }
            }
        }
        if(funcaoDadosSecundaria != null){
            row.getCell(19).setCellValue(this.pegarValorValidacaoDuasFuncao(funcaoDadosPrimaria, funcaoDadosSecundaria));
            row.getCell(20).setCellValue(funcaoDadosSecundaria.getFatorAjuste().getNome());
            row.getCell(22).setCellValue(funcaoDadosSecundaria.getTipo().toString());
            String dersSecundaria = funcaoDadosSecundaria.getDers().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(23).setCellValue(this.getTotalDer(funcaoDadosSecundaria.getDers()));
            row.getCell(24).setCellValue(dersSecundaria);
            row.getCell(25).setCellValue(this.getTotalRlr(funcaoDadosSecundaria.getRlrs()));
            String rlrsSecundaria = funcaoDadosSecundaria.getRlrs().stream().map(item -> item.getNome()).collect(Collectors.joining(", "));
            row.getCell(26).setCellValue(rlrsSecundaria);
            row.getCell(33).setCellValue(funcaoDadosSecundaria.getLstDivergenceComments().stream().map(item -> item.getComment()).collect(Collectors.joining(", ")));
        }
    }


    private boolean testarFuncaoDadoDivergencia(FuncaoDados funcaoDadosPrimaria, FuncaoDados funcaoDadosSecundaria) {
        if((funcaoDadosSecundaria != null && funcaoDadosPrimaria.getName() != null
            && funcaoDadosPrimaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)
            && funcaoDadosSecundaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)) ||
            funcaoDadosSecundaria == null && funcaoDadosPrimaria.getName() != null
                && funcaoDadosPrimaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO) ||
            funcaoDadosSecundaria != null && funcaoDadosPrimaria.getName() == null
                && funcaoDadosSecundaria.getStatusFuncao().equals(StatusFuncao.EXCLUIDO)){
            return true;
        }
        return false;
    }

    private void carregarFuncoesDivergencia(List<FuncaoDados> funcaoDadosList, List<FuncaoTransacao> funcaoTransacaoList, Map<FuncaoDados, FuncaoDados> funcaoDados, Map<FuncaoTransacao, FuncaoTransacao> funcaoTransacao) {
        this.carregarFuncoesDadosDivergencia(funcaoDadosList, funcaoDados);
        this.carregarFuncoesTransacaoDivergencia(funcaoTransacaoList, funcaoTransacao);
    }

    private void carregarFuncoesTransacaoDivergencia(List<FuncaoTransacao> funcaoTransacaoList, Map<FuncaoTransacao, FuncaoTransacao> funcaoTransacao) {
        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcao = funcaoTransacaoList.get(i);
            if(funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO)){
                funcaoTransacao.put(funcao, null);
                for (int j = 0; j < funcaoTransacaoList.size(); j++) {
                    FuncaoTransacao funcaoSecundaria = funcaoTransacaoList.get(j);
                    if(!funcaoSecundaria.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && isFuncaoEquiparada(funcao, funcaoSecundaria) == true){
                        funcaoTransacao.put(funcao, funcaoSecundaria);
                    }
                }
            }
        }

        for (int i = 0; i < funcaoTransacaoList.size(); i++) {
            FuncaoTransacao funcao = funcaoTransacaoList.get(i);
            if(!funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && !funcaoTransacao.containsValue(funcao)){
                FuncaoTransacao novaFuncao = new FuncaoTransacao();
                novaFuncao.setId(Long.valueOf(i));
                funcaoTransacao.put(novaFuncao, funcao);
            }
        }
    }

    private void carregarFuncoesDadosDivergencia(List<FuncaoDados> funcaoDadosList, Map<FuncaoDados, FuncaoDados> funcaoDados) {
        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcao = funcaoDadosList.get(i);
            if(funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO)){
                funcaoDados.put(funcao, null);
                for (int j = 0; j < funcaoDadosList.size(); j++) {
                    FuncaoDados funcaoSecundaria = funcaoDadosList.get(j);
                    if(!funcaoSecundaria.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && isFuncaoEquiparada(funcao, funcaoSecundaria) == true){
                        funcaoDados.put(funcao, funcaoSecundaria);
                    }
                }
            }
        }

        for (int i = 0; i < funcaoDadosList.size(); i++) {
            FuncaoDados funcao = funcaoDadosList.get(i);
            if(!funcao.getEquipe().getNome().toLowerCase().contains(BASIS_MINUSCULO) && !funcaoDados.containsValue(funcao)){
                FuncaoDados novaFuncao = new FuncaoDados();
                novaFuncao.setId(Long.valueOf(i));
                funcaoDados.put(novaFuncao, funcao);
            }
        }
    }

    private String pegarValorValidacaoDuasFuncao(FuncaoDados funcaoPrimaria, FuncaoDados funcaoSecundaria) {
        if(funcaoPrimaria.getName() != null && funcaoPrimaria.getStatusFuncao().equals(StatusFuncao.VALIDADO) ||
            funcaoSecundaria != null && funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.VALIDADO)){
            return "OK";
        }else if(funcaoPrimaria.getName() != null && funcaoPrimaria.getStatusFuncao().equals(StatusFuncao.DIVERGENTE) ||
            funcaoSecundaria != null && funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.DIVERGENTE)){
            return DIVERGENTE;
        }else {
            return "";
        }
    }


    private String pegarValorValidacaoDuasFuncao(FuncaoTransacao funcaoPrimaria, FuncaoTransacao funcaoSecundaria) {
        if(funcaoPrimaria.getName() != null && funcaoPrimaria.getStatusFuncao().equals(StatusFuncao.VALIDADO) ||
            funcaoSecundaria != null && funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.VALIDADO)){
            return "OK";
        }else if(funcaoPrimaria.getName() != null && funcaoPrimaria.getStatusFuncao().equals(StatusFuncao.DIVERGENTE) ||
            funcaoSecundaria != null && funcaoSecundaria.getStatusFuncao().equals(StatusFuncao.DIVERGENTE)){
            return DIVERGENTE;
        }else {
            return "";
        }
    }


    private boolean isFuncaoEquiparada(FuncaoDados funcaoPrimaria, FuncaoDados funcaoSecundaria) {
        if(funcaoPrimaria.getName().equals(funcaoSecundaria.getName()) &&
            funcaoPrimaria.getFuncionalidade().getNome().equals(funcaoSecundaria.getFuncionalidade().getNome()) &&
            funcaoPrimaria.getFuncionalidade().getModulo().getNome().equals(funcaoSecundaria.getFuncionalidade().getModulo().getNome())){
            return true;
        }else{
            return false;
        }
    }

    private boolean isFuncaoEquiparada(FuncaoTransacao funcaoPrimaria, FuncaoTransacao funcaoSecundaria) {
        if(funcaoPrimaria.getName().equals(funcaoSecundaria.getName()) &&
            funcaoPrimaria.getFuncionalidade().getNome().equals(funcaoSecundaria.getFuncionalidade().getNome()) &&
            funcaoPrimaria.getFuncionalidade().getModulo().getNome().equals(funcaoSecundaria.getFuncionalidade().getModulo().getNome())){
            return true;
        }else{
            return false;
        }
    }


    public Integer getTotalRlr(Set<Rlr> listaRlr){
        Integer total = listaRlr.size();
        if(total == 1 && listaRlr.iterator().next().getValor() != null){
            total = listaRlr.iterator().next().getValor();
        }
        return total;
    }
    public Integer getTotalAlr(Set<Alr> listaAlr){
        Integer total = listaAlr.size();
        if(total == 1 && listaAlr.iterator().next().getValor() != null){
            total = listaAlr.iterator().next().getValor();
        }
        return total;
    }
    public Integer getTotalDer(Set<Der> listaDer){
        Integer total = listaDer.size();
        if(total == 1 && listaDer.iterator().next().getValor() != null){
            total = listaDer.iterator().next().getValor();
        }
        return total;
    }

}
