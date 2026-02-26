package br.com.basis.abaco.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.service.dto.DropdownDTO;

/**
 * Spring Data JPA repository for the Funcionalidade entity.
 */
public interface FuncionalidadeRepository extends JpaRepository<Funcionalidade, Long> {

    @Query(value = "SELECT new br.com.basis.abaco.service.dto.DropdownDTO(f.id, f.nome) FROM Funcionalidade f WHERE f.modulo.id = :idModulo ")
    List<DropdownDTO> findDropdownByModuloId(@Param("idModulo") Long idModulo);

    Optional<List<Funcionalidade>> findAllByNomeIgnoreCaseAndModuloId(String nome, Long moduloId);
    
    Optional<List<Funcionalidade>> findByModuloId(Long moduloId);

    /**
     * Resolve o ID de uma funcionalidade a partir do nome do sistema, módulo e funcionalidade.
     * Evita carregar o grafo completo do Sistema apenas para resolver um ID.
     *
     * @return ID da funcionalidade, ou null se não encontrada
     */
    @Query(value =
        "SELECT f.id FROM funcionalidade f " +
        "INNER JOIN modulo m ON f.modulo_id = m.id " +
        "WHERE m.sistema_id = :sistemaId " +
        "AND m.nome = :nomeModulo " +
        "AND f.nome = :nomeFuncionalidade " +
        "LIMIT 1",
        nativeQuery = true)
    Long findIdPorSistemaModuloFuncionalidade(
        @Param("sistemaId") Long sistemaId,
        @Param("nomeModulo") String nomeModulo,
        @Param("nomeFuncionalidade") String nomeFuncionalidade);
}
