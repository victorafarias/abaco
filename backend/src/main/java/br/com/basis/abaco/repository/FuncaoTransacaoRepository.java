package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for the FuncaoTransacao entity.
 */
@SuppressWarnings("unused")
public interface FuncaoTransacaoRepository extends JpaRepository<FuncaoTransacao, Long> {

    @Query(value = "SELECT f.funcionalidade.id FROM FuncaoTransacao f where f.id = ?1")
    Long getIdFuncionalidade(Long id);

    @Query("SELECT f FROM FuncaoTransacao f JOIN FETCH f.ders WHERE f.id = (:id)")
    FuncaoTransacao findWithDerAndAlr(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"funcionalidade", "alrs", "ders", "fatorAjuste"})
    FuncaoTransacao findOne(@Param("id") Long id);

    @Query(value = "SELECT f FROM FuncaoTransacao f WHERE f.funcionalidade.id = :id")
    Set<FuncaoTransacao> findByFuncionalidade(@Param("id") Long id);

    @Query(value = "SELECT f FROM FuncaoTransacao f WHERE f.analise.id = :analiseId AND f.funcionalidade.id = :funcionalidadeId ORDER BY f.name asc, f.id asc")
    Set<FuncaoTransacao> findByAnaliseFuncionalidade(@Param("analiseId") Long analiseId, @Param("funcionalidadeId") Long funcionalidadeId);

    @EntityGraph(attributePaths = {"funcionalidade", "alrs", "ders", "fatorAjuste"})
    @Query("SELECT ft FROM FuncaoTransacao ft WHERE ft.analise.id = ?1")
    Set<FuncaoTransacao> findAllByAnaliseId(Long id);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloId(String name, Long analiseId, Long idFuncionalidade, Long idModulo);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndIdNot(String name, Long analiseId, Long idFuncionalidade, Long idModulo, Long id);

    Set<FuncaoTransacao> findByAnaliseIdOrderByFuncionalidadeModuloNomeAscFuncionalidadeNomeAscNameAsc(Long id);


    @EntityGraph(attributePaths = {"funcionalidade", "alrs", "ders", "fatorAjuste"})
    Set<FuncaoTransacao> findAllByAnaliseIdOrderByOrdem(Long idAnalise);

    Set<FuncaoTransacao> findByAnaliseIdAndStatusFuncaoOrderByOrdem(Long id, StatusFuncao statusFuncao);

    @EntityGraph(attributePaths = {"funcionalidade", "rlrs", "ders", "fatorAjuste"})
    Set<FuncaoTransacao> findByAnaliseIdAndStatusFuncaoNotOrderByOrdem(Long id, StatusFuncao statusFuncao);

    Set<FuncaoTransacao> findByAnaliseIdAndStatusFuncaoOrderByFuncionalidadeModuloNomeAscFuncionalidadeNomeAscNameAsc(Long id, StatusFuncao statusFuncao);

    long countByFuncionalidadeId(Long id);

    Optional<List<FuncaoTransacao>> findAllByFuncionalidadeId(Long id);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndIdNotAndEquipeId(String name, Long idAnalise, Long idfuncionalidade, Long idModulo, Long id, Long idEquipe);

    Boolean existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndEquipeId(String name, Long idAnalise, Long idfuncionalidade, Long idModulo, Long idEquipe);
}
