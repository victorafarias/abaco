package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.NovidadesVersao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * Spring Data JPA repository for the Novidade entity.
 */
@SuppressWarnings("unused")
public interface NovidadesVersaoRepository extends JpaRepository<NovidadesVersao, Long> {

    @Query("SELECT nv FROM NovidadesVersao nv join fetch nv.novidades ORDER BY nv.id")
    Set<NovidadesVersao> findAllOrderById();
}
