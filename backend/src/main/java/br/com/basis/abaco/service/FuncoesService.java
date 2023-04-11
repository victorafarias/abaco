package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoDadosVersionavel;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.FuncaoDadosVersionavelRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.search.FuncaoDadosSearchRepository;
import br.com.basis.abaco.repository.search.FuncaoTransacaoSearchRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class FuncoesService {

    private final FuncaoDadosRepository funcaoDadosRepository;
    private final FuncaoDadosSearchRepository funcaoDadosSearchRepository;
    private final FuncaoTransacaoRepository funcaoTransacaoRepository;
    private final FuncaoTransacaoSearchRepository funcaoTransacaoSearchRepository;
    private final FuncaoDadosVersionavelRepository funcaoDadosVersionavelRepository;

    public FuncoesService(FuncaoDadosRepository funcaoDadosRepository,
                          FuncaoDadosSearchRepository funcaoDadosSearchRepository,
                          FuncaoTransacaoRepository funcaoTransacaoRepository,
                          FuncaoTransacaoSearchRepository funcaoTransacaoSearchRepository,
                          FuncaoDadosVersionavelRepository funcaoDadosVersionavelRepository) {
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.funcaoDadosSearchRepository = funcaoDadosSearchRepository;
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
        this.funcaoTransacaoSearchRepository = funcaoTransacaoSearchRepository;
        this.funcaoDadosVersionavelRepository = funcaoDadosVersionavelRepository;
    }

    public Set<FuncaoDados> obterFuncaoDadosPorAnaliseId(Long idAnalise) {
        return funcaoDadosRepository.findAllByAnaliseIdOrderByOrdem(idAnalise);
    }

    public Set<FuncaoTransacao> obterFuncaoTransacaoPorAnaliseId(Long idAnalise) {
        return funcaoTransacaoRepository.findAllByAnaliseIdOrderByOrdem(idAnalise);
    }

    public Set<FuncaoDados> obterFuncaoDadosPorAnaliseIdStatusFuncao(Long idAnalise) {
        return funcaoDadosRepository.findByAnaliseIdAndStatusFuncaoNotOrderByOrdem(idAnalise, StatusFuncao.EXCLUIDO);
    }

    public Set<FuncaoTransacao> obterFuncaoTransacaoPorAnaliseIdStatusFuncao(Long idAnalise) {
        return funcaoTransacaoRepository.findByAnaliseIdAndStatusFuncaoNotOrderByOrdem(idAnalise, StatusFuncao.EXCLUIDO);
    }

    public Optional<FuncaoDadosVersionavel> obterFuncaoDadoVersionavelPorNomeSistemaId(String nome, Long idSistema) {
        return funcaoDadosVersionavelRepository.findOneByNomeIgnoreCaseAndSistemaId(nome, idSistema);
    }

    public void salvarFuncaoDado(FuncaoDados funcaoDados) {
        funcaoDadosRepository.save(funcaoDados);
        funcaoDadosSearchRepository.save(funcaoDados);
    }

    public void salvarFuncaoTransacao(FuncaoTransacao funcaoTransacao) {
        funcaoTransacaoRepository.save(funcaoTransacao);
        funcaoTransacaoSearchRepository.save(funcaoTransacao);
    }

    public FuncaoDadosVersionavel salvarFuncaoDadosVersionavel(FuncaoDadosVersionavel funcaoDadosVersionavel) {
        return funcaoDadosVersionavelRepository.save(funcaoDadosVersionavel);
    }
}
