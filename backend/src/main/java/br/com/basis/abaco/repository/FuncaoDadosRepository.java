package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.service.dto.DropdownDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the FuncaoDados entity.
 */
public interface FuncaoDadosRepository extends JpaRepository<FuncaoDados, Long> {


    Optional<FuncaoDados> findFirstByFuncaoDadosVersionavelIdOrderByAuditUpdatedOnDesc(
        Long funcaoDadosVersionavelId);

    @Query(value = "SELECT f FROM FuncaoDados f WHERE f.analise.id = ?1 AND f.name = ?2")
    FuncaoDados findName(Long idAnalise, String name);

    @Query(value = "SELECT f FROM FuncaoDados f WHERE f.analise.id = ?1 ")
    List<FuncaoDados> findByAnalise(Long id);

    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    @Query(value = "SELECT f FROM FuncaoDados f WHERE f.id = ?1")
    FuncaoDados findById(Long id);

    @Query(value = "SELECT f.funcionalidade.id FROM FuncaoDados f where f.id = ?1")
    Long getIdFuncionalidade(Long id);

    @Override
    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    FuncaoDados findOne(@Param("id") Long id);

    @Query(value = "SELECT f FROM FuncaoDados f WHERE f.analise.id = :analiseId AND f.funcionalidade.id = :funcionalidadeId ORDER BY f.name asc, f.id asc")
    Set<FuncaoDados> findByAnaliseFuncionalidade(@Param("analiseId") Long analiseId, @Param("funcionalidadeId") Long funcionalidadeId);

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.DropdownDTO(f.id, f.name) FROM Analise a JOIN a.funcaoDados f"
        + " WHERE a.enviarBaseline = true AND a.bloqueiaAnalise = true")
    List<DropdownDTO> getFuncaoDadosDropdown();


    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    @Query("SELECT fd FROM FuncaoDados fd WHERE fd.analise.id = :idAnalise Order by fd.funcionalidade.modulo.nome ")
    Set<FuncaoDados> findByAnaliseId(@Param("idAnalise") Long idAnalise);

    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    Set<FuncaoDados> findAllByAnaliseIdOrderByOrdem(Long idAnalise);

    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    Set<FuncaoDados> findByAnaliseIdAndStatusFuncaoNotOrderByOrdem(Long id, StatusFuncao statusFuncao);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloId(String name, Long analiseId, Long idFuncionalidade, Long idModulo);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndIdNot(String name, Long analiseId, Long idFuncionalidade, Long idModulo, Long id);


    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndEquipeId(String name, Long analiseId, Long idFuncionalidade, Long idModulo, Long equipeId);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndIdNotAndEquipeId(String name, Long analiseId, Long idFuncionalidade, Long idModulo, Long id, Long equipeId);

    long countByFuncionalidadeId(Long id);

    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    FuncaoDados findByIdOrderByDersIdAscRlrsIdAsc(Long id);

    Optional<List<FuncaoDados>> findAllByFuncionalidadeId(Long id);

    // Alterado: Método para buscar a maior ordem de funções de dados de uma análise
    @Query("SELECT COALESCE(MAX(fd.ordem), 0) FROM FuncaoDados fd WHERE fd.analise.id = :analiseId")
    Long findMaxOrdemByAnaliseId(@Param("analiseId") Long analiseId);

    /**
     * Busca funções distintas de dados de um sistema.
     * Retorna apenas os campos necessários (sem EntityGraph) usando query SQL otimizada.
     * Esta query elimina o problema N+1 e reduz significativamente o volume de dados transferidos.
     * 
     * @param sistemaId ID do sistema
     * @return Lista de arrays [nomeModulo, nomeFuncionalidade, nomeFuncao, tipo]
     */
    @Query(value = 
        "SELECT DISTINCT " +
        "  m.nome AS nomeModulo, " +
        "  f.nome AS nomeFuncionalidade, " +
        "  fd.name AS nomeFuncao, " +
        "  'FD' AS tipo " +
        "FROM funcao_dados fd " +
        "INNER JOIN funcionalidade f ON fd.funcionalidade_id = f.id " +
        "INNER JOIN modulo m ON f.modulo_id = m.id " +
        "INNER JOIN analise a ON fd.analise_id = a.id " +
        "WHERE a.sistema_id = :sistemaId " +
        "ORDER BY m.nome, f.nome, fd.name",
        nativeQuery = true)
    List<Object[]> findFuncoesDistintasDadosBySistemaId(@Param("sistemaId") Long sistemaId);
}
