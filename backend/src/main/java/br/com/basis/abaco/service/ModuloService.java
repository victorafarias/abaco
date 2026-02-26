package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.FuncionalidadeRepository;
import br.com.basis.abaco.repository.ModuloRepository;
import br.com.basis.abaco.repository.search.FuncionalidadeSearchRepository;
import br.com.basis.abaco.repository.search.ModuloSearchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ModuloService {

    private final ModuloRepository moduloRepository;
    private final ModuloSearchRepository moduloSearchRepository;
    private final FuncionalidadeRepository funcionalidadeRepository;
    private final FuncionalidadeSearchRepository funcionalidadeSearchRepository;
    private final FuncaoDadosRepository funcaoDadosRepository;
    private final FuncaoTransacaoRepository funcaoTransacaoRepository;

    public ModuloService(ModuloRepository moduloRepository, ModuloSearchRepository moduloSearchRepository, 
                         FuncionalidadeRepository funcionalidadeRepository, FuncionalidadeSearchRepository funcionalidadeSearchRepository,
                         FuncaoDadosRepository funcaoDadosRepository, FuncaoTransacaoRepository funcaoTransacaoRepository) {
        this.moduloRepository = moduloRepository;
        this.moduloSearchRepository = moduloSearchRepository;
        this.funcionalidadeRepository = funcionalidadeRepository;
        this.funcionalidadeSearchRepository = funcionalidadeSearchRepository;
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
    }

    public Long countTotalFuncao(Long id) {
        return funcaoDadosRepository.countByFuncionalidadeModuloId(id) +
               funcaoTransacaoRepository.countByFuncionalidadeModuloId(id);
    }

    public void migrarFuncionalidades(Long idEdit, Long idMigrar) {
        Modulo moduloOrigem = moduloRepository.findOne(idEdit);
        Modulo moduloDestino = moduloRepository.findOne(idMigrar);

        // Transferir funcionalidades entre as coleções JPA gerenciadas.
        // Isso evita que orphanRemoval=true tente deletar funcionalidades
        // que ainda possuem funcao_dados/funcao_transacao referenciadas por FK.
        List<Funcionalidade> funcionalidadesParaMigrar = new java.util.ArrayList<>(moduloOrigem.getFuncionalidades());
        for (Funcionalidade f : funcionalidadesParaMigrar) {
            moduloOrigem.removeFuncionalidade(f);
            moduloDestino.addFuncionalidade(f);
        }

        moduloRepository.save(moduloDestino);
        moduloRepository.flush();

        // Agora o módulo origem está sem funcionalidades — pode ser deletado com segurança
        moduloRepository.delete(idEdit);
        moduloSearchRepository.delete(idEdit);
    }
}
