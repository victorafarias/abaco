package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.PEAnaliticoEstimada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PEAnaliticoEstimadaRepository extends JpaRepository<PEAnaliticoEstimada, Long> {

    Set<PEAnaliticoEstimada> findAllByidsistemaAndTipoAndEquipeResponsavelIdOrderByName(Long idsistema, String tipo, Long equipeResponsavelId);

    Set<PEAnaliticoEstimada> findByIdFuncionalidadeAndTipoAndNameContainingIgnoreCaseOrderByName(Long idFuncionalidade, String tipo, String name);

    Set<PEAnaliticoEstimada> findAllByIdModuloAndTipoAndEquipeResponsavelIdOrderByName(Long idModulo, String tipo, Long equipeResponsavelId);

    Set<PEAnaliticoEstimada> findAllByIdFuncionalidadeAndTipoAndEquipeResponsavelIdOrderByName(Long idFuncionalidade, String tipo, Long equipeResponsavelId);

    Set<PEAnaliticoEstimada> findAllByIdModuloAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idmodulo, String tipo, String name, Long equipeResponsavelId);

    Set<PEAnaliticoEstimada> findAllByIdFuncionalidadeAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idFuncionalidade, String tipo, String name, Long equipeResponsavelId);

    Set<PEAnaliticoEstimada> findAllByidsistemaAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idsistema, String tipo, String name, Long equipeResponsavelId);
}
