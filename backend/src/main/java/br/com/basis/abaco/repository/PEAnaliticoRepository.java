package br.com.basis.abaco.repository;

import br.com.basis.abaco.domain.PEAnalitico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PEAnaliticoRepository extends JpaRepository<PEAnalitico, Long> {

    Set<PEAnalitico> findAllByidsistemaAndTipoAndEquipeResponsavelIdOrderByName(Long idsistema, String tipo, Long equipeResponsavelId);

    Set<PEAnalitico> findByIdFuncionalidadeAndTipoAndNameContainingIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idFuncionalidade, String tipo, String name, Long equipeResponsavelId);

    Set<PEAnalitico> findAllByIdModuloAndTipoAndEquipeResponsavelIdOrderByName(Long idModulo, String tipo, Long equipeResponsavelId);

    Set<PEAnalitico> findAllByIdFuncionalidadeAndTipoAndEquipeResponsavelIdOrderByName(Long idFuncionalidade, String tipo, Long equipeResponsavelId);

    Set<PEAnalitico> findAllByIdModuloAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idmodulo, String tipo, String name, Long equipeResponsavelId);

    Set<PEAnalitico> findAllByIdFuncionalidadeAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idFuncionalidade, String tipo, String name, Long equipeResponsavelId);

    Set<PEAnalitico> findAllByidsistemaAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(Long idsistema, String tipo, String name, Long equipeResponsavelId);
}
