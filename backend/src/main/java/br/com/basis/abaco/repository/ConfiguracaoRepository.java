package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.Configuracao;
import br.com.basis.abaco.domain.Der;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Configuracao entity.
 */
public interface ConfiguracaoRepository extends JpaRepository<Configuracao, Long> {

}
