package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.Historico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the Historico entity.
 */
@SuppressWarnings("unused")
public interface HistoricoRepository extends JpaRepository<Historico,Long> {

    List<Historico> findAllByAnaliseIdOrderById(Long idAnalise);

}
