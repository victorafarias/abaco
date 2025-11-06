package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.BatchJobExecution;
import br.com.basis.abaco.service.dto.BatchJobExecutionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdministracaoRepository extends JpaRepository<BatchJobExecution, Long> {

    @Query(value = "select new br.com.basis.abaco.service.dto.BatchJobExecutionDTO(rotina) from BatchJobExecution rotina")
    List<BatchJobExecutionDTO> obterTodasRotinas();

}
