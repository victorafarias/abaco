package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.PEAnalitico;
import br.com.basis.abaco.domain.PEAnaliticoEstimada;
import br.com.basis.abaco.repository.PEAnaliticoEstimadaRepository;
import br.com.basis.abaco.repository.PEAnaliticoRepository;
import br.com.basis.abaco.service.dto.PEAnaliticoDTO;
import br.com.basis.abaco.utils.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PEAnaliticoService {

    private final PEAnaliticoRepository peAnaliticoRepository;

    private final ModelMapper modelMapper;

    private final PEAnaliticoEstimadaRepository peAnaliticoEstimadaRepository;

    public PEAnaliticoService(PEAnaliticoRepository peAnaliticoRepository, ModelMapper modelMapper, PEAnaliticoEstimadaRepository peAnaliticoEstimadaRepository) {
        this.peAnaliticoRepository = peAnaliticoRepository;
        this.modelMapper = modelMapper;
        this.peAnaliticoEstimadaRepository = peAnaliticoEstimadaRepository;
    }

    public PEAnalitico convertToEntity(PEAnaliticoDTO peAnaliticoDTO) {
        return modelMapper.map(peAnaliticoDTO, PEAnalitico.class);
    }

    public PEAnaliticoDTO convertToPEAnaliticoDTO(PEAnalitico peAnalitico) {
        return modelMapper.map(peAnalitico, PEAnaliticoDTO.class);
    }

    public PEAnaliticoDTO convertToPEAnaliticoDTO(PEAnaliticoEstimada peAnalitico) {
        return modelMapper.map(peAnalitico, PEAnaliticoDTO.class);
    }

    @Transactional
    public Set<PEAnaliticoDTO> getPeAnaliticoDTOS(Long idModulo, Long idFuncionalidade, String name, Long idSistema, String tipo, Long idEquipeResponsavel) {
        Set<PEAnalitico> lstPeAnaliticos;
        if (idFuncionalidade != null && idFuncionalidade > 0) {
            if(StringUtils.isEmptyString(name)){
                lstPeAnaliticos = peAnaliticoRepository.findAllByIdFuncionalidadeAndTipoAndEquipeResponsavelIdOrderByName(idFuncionalidade, tipo, idEquipeResponsavel);
            } else {
                lstPeAnaliticos = peAnaliticoRepository.findAllByIdFuncionalidadeAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(idFuncionalidade, tipo, name, idEquipeResponsavel);
            }
        } else if(idModulo != null && idModulo > 0){
            if(StringUtils.isEmptyString(name)){
                lstPeAnaliticos = peAnaliticoRepository.findAllByIdModuloAndTipoAndEquipeResponsavelIdOrderByName(idModulo, tipo, idEquipeResponsavel);
            } else {
                lstPeAnaliticos = peAnaliticoRepository.findAllByIdModuloAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(idModulo, tipo, name, idEquipeResponsavel);
            }
        } else {
            if(StringUtils.isEmptyString(name)){
                lstPeAnaliticos = peAnaliticoRepository.findAllByidsistemaAndTipoAndEquipeResponsavelIdOrderByName(idSistema, tipo, idEquipeResponsavel);
            } else {
                lstPeAnaliticos = peAnaliticoRepository.findAllByidsistemaAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(idSistema, tipo, name, idEquipeResponsavel);
            }
        }
        return lstPeAnaliticos.stream().map(this::convertToPEAnaliticoDTO).collect(Collectors.toSet());
    }

    @Transactional
    public Set<PEAnaliticoDTO> getPeAnaliticoEstimadaDTOS(Long idModulo, Long idFuncionalidade, String name, Long idSistema, String tipo, Long idEquipeResponsavel) {
        Set<PEAnaliticoEstimada> lstPeAnaliticos;
        if (idFuncionalidade != null && idFuncionalidade > 0) {
            if(StringUtils.isEmptyString(name)){
                lstPeAnaliticos = peAnaliticoEstimadaRepository.findAllByIdFuncionalidadeAndTipoAndEquipeResponsavelIdOrderByName(idFuncionalidade, tipo, idEquipeResponsavel);
            } else {
                lstPeAnaliticos = peAnaliticoEstimadaRepository.findAllByIdFuncionalidadeAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(idFuncionalidade, tipo, name, idEquipeResponsavel);
            }
        } else if(idModulo != null && idModulo > 0){
            if(StringUtils.isEmptyString(name)){
                lstPeAnaliticos = peAnaliticoEstimadaRepository.findAllByIdModuloAndTipoAndEquipeResponsavelIdOrderByName(idModulo, tipo, idEquipeResponsavel);
            } else {
                lstPeAnaliticos = peAnaliticoEstimadaRepository.findAllByIdModuloAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(idModulo, tipo, name, idEquipeResponsavel);
            }
        } else {
            if(StringUtils.isEmptyString(name)){
                lstPeAnaliticos = peAnaliticoEstimadaRepository.findAllByidsistemaAndTipoAndEquipeResponsavelIdOrderByName(idSistema, tipo, idEquipeResponsavel);
            } else {
                lstPeAnaliticos = peAnaliticoEstimadaRepository.findAllByidsistemaAndTipoAndNameContainsIgnoreCaseAndEquipeResponsavelIdOrderByName(idSistema, tipo, name, idEquipeResponsavel);
            }
        }
        return lstPeAnaliticos.stream().map(this::convertToPEAnaliticoDTO).collect(Collectors.toSet());
    }
}
