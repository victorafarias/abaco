package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Sistema;
import br.com.basis.abaco.service.dto.Dashboard2DTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Analise entity.
 */
@SuppressWarnings("unused")
public interface AnaliseRepository extends JpaRepository<Analise, Long> {


    @Query(value = "SELECT a FROM Analise a WHERE a.createdBy.id = ?1")
    List<Analise> findByCreatedBy(Long userid);

    @Query(value = "SELECT a FROM Analise a WHERE a.id IN :idAnalise")
    Page<Analise> findById(@Param("idAnalise") List<Long> idAnalises, Pageable pageable);

    @Query(value = "SELECT a.id FROM Analise a WHERE a.equipeResponsavel.id IN :equipes")
    List<Long> findAllByTipoEquipesId(@Param("equipes") List<Long> equipes);

    @Query(value = "SELECT a FROM Analise a WHERE a.enviarBaseline = true AND a.bloqueiaAnalise = true")
    List<Analise> findAllByBaseline();

    @Query(value = "SELECT a FROM Analise a WHERE a.id IN :idAnalise")
    Page<Analise> findByIds(@Param("idAnalise") List<Long> idAnalise, Pageable pageable);

    @Query(value = "SELECT count(*) FROM Analise a WHERE a.equipeResponsavel.id IN :equipes AND a.id = :idAnalise")
    int analiseEquipe(@Param("idAnalise") Long idAnalise, @Param("equipes") List<Long> equipes);

    @Query(value = "SELECT a.viewOnly FROM Compartilhada a WHERE a.analiseId = ?1")
    Boolean analiseCompartilhada(Long analiseId);

    @EntityGraph(attributePaths = {"compartilhadas", "funcaoDados", "funcaoTransacaos", "esforcoFases", "users", "fatorAjuste", "contrato"})
    Analise findOne(Long id);

    @Query(value = "SELECT a FROM Analise a WHERE a.id = :id")
    Analise findOneByIdClean(@Param("id") Long id);

    @EntityGraph(attributePaths = {"compartilhadas", "esforcoFases", "users", "fatorAjuste", "contrato"})
    Analise findById(Long id);

    @EntityGraph(attributePaths = {"compartilhadas", "esforcoFases", "users", "fatorAjuste", "contrato"})
    List<Analise> findAll();

    @EntityGraph(attributePaths = {"compartilhadas", "esforcoFases", "users", "fatorAjuste", "contrato", "analisesComparadas"})
    Analise findOneById(Long id);

    @Query(value = "SELECT a " +
        "FROM Analise a " +
        "JOIN Sistema s              ON s.id = a.sistema.id " +
        "JOIN Organizacao o          ON o.id = a.organizacao.id " +
        "JOIN Modulo m               ON s.id = m.sistema.id " +
        "JOIN Funcionalidade f       ON f.modulo.id = m.id " +
        "JOIN FuncaoDados fd        ON fd.funcionalidade.id = f.id " +
        "JOIN FuncaoTransacao ft    ON ft.funcionalidade.id = f.id " +
        "JOIN FETCH FatorAjuste fa        ON fa.id = fd.fatorAjuste.id OR fa.id = ft.fatorAjuste.id " +
        "WHERE a.id = :id ORDER BY m.nome, f.nome, fd.name, ft.name")
    Analise reportContagem(@Param("id") Long id);


    @Query(value = "SELECT a FROM Analise a WHERE a.isDivergence = :divergencia")
    Page<Analise> pesquisarPorDivergencia(@Param("divergencia") Boolean divergencia, Pageable pageable);

    List<Analise> findAllBySistema(Sistema sistema);

    @Query(value = "SELECT a FROM Analise a WHERE a.equipeResponsavel.id = :equipeId AND a.sistema.id = :sistemaId")
    List<Analise> findBySistemaAndEquipe(@Param("equipeId") Long equipeId, @Param("sistemaId") Long sistemaId);

    @Query("select analise " +
        "from Analise analise " +
        "join analise.funcaoDados funcaoDados " +
        "join analise.sistema sistema " +
        "join analise.equipeResponsavel equipe " +
        "join funcaoDados.funcionalidade funcionalidade " +
        "join funcionalidade.modulo modulo " +
        "where funcaoDados.name like :nomeFuncao " +
        "and modulo.nome like :nomeModulo " +
        "and funcionalidade.nome like :nomeFuncionalidade " +
        "and sistema.nome like :nomeSistema " +
        "and equipe.nome like :nomeEquipe ")
    List<Analise> obterPorFuncaoDados(@Param("nomeFuncao") String nomeFuncao,
                                         @Param("nomeModulo") String nomeModulo,
                                         @Param("nomeFuncionalidade") String nomeFuncionalidade,
                                         @Param("nomeSistema") String nomeSistema,
                                         @Param("nomeEquipe") String nomeEquipe);

    @Query("select analise " +
        "from Analise analise " +
        "join analise.funcaoTransacaos funcaoTransacao " +
        "join analise.sistema sistema " +
        "join analise.equipeResponsavel equipe " +
        "join funcaoTransacao.funcionalidade funcionalidade " +
        "join funcionalidade.modulo modulo " +
        "where funcaoTransacao.name like :nomeFuncao " +
        "and modulo.nome like :nomeModulo " +
        "and funcionalidade.nome like :nomeFuncionalidade " +
        "and sistema.nome like :nomeSistema " +
        "and equipe.nome like :nomeEquipe ")
    List<Analise> obterPorFuncaoTransacao(@Param("nomeFuncao") String nomeFuncao,
                                             @Param("nomeModulo") String nomeModulo,
                                             @Param("nomeFuncionalidade") String nomeFuncionalidade,
                                             @Param("nomeSistema") String nomeSistema,
                                             @Param("nomeEquipe") String nomeEquipe);


    @Query(nativeQuery = true, value = "select analise.* from analise analise " +
        "join organizacao on organizacao.id = analise.organizacao_id " +
        "join status_analise status on status.id = analise.status_id " +
        "where status.nome != 'Aprovada' " +
        "and analise.is_divergence = false " +
        "and analise.analise_divergence_id is null " +
        "and analise.data_criacao_ordem_servico < (date(analise.data_criacao_ordem_servico) + organizacao.prazo_aprovacao_divergencia_dias)")
    List<Analise> obterAnalisesDivergenciaForaDoPrazo();

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.Dashboard2DTO(analise.motivo, COUNT(*)) " +
        "FROM Analise analise " +
        "WHERE analise.motivo IS NOT null " +
        "AND analise.status.id = 1805353 " +
        "GROUP BY analise.motivo ")
    List<Dashboard2DTO> getMotivosAnalise();

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.Dashboard2DTO(o.sigla, COUNT(*)) " +
        "FROM Analise analise " +
        "JOIN Organizacao o on o.id = analise.organizacao.id " +
        "WHERE analise.status.id = 1805353 GROUP BY o.sigla")
    List<Dashboard2DTO> getClientesAnalise();

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.Dashboard2DTO(SUM(a.pfTotalOriginal - a.pfTotalAprovado), to_char(date(a.dataCriacaoOrdemServico), 'dd/MM'))" +
        " FROM Analise a " +
        "WHERE a.status.id = 1805353 " +
        "AND a.pfTotalOriginal IS NOT NULL " +
        "AND a.pfTotalAprovado IS NOT NULL " +
        "GROUP BY date(a.dataCriacaoOrdemServico) " +
        "ORDER BY date(a.dataCriacaoOrdemServico)")
    List<Dashboard2DTO> getHistoricoDiferenca();

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.Dashboard2DTO(COUNT(*)) " +
        "FROM Analise a " +
        "WHERE a.status.id = 1805353 " +
        "AND a.pfTotalOriginal IS NOT NULL " +
        "AND a.pfTotalAprovado IS NOT NULL")
    List<Dashboard2DTO> getTotalDemandas();

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.Dashboard2DTO(SUM(a.pfTotalOriginal - a.pfTotalAprovado))" +
        " FROM Analise a " +
        "WHERE a.status.id = 1805353 " +
        "AND a.pfTotalOriginal IS NOT NULL " +
        "AND a.pfTotalAprovado IS NOT NULL")
    List<Dashboard2DTO> getHistoricoDiferencaGlobal();

}
