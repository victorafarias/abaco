package br.com.basis.abaco.service.mapper;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.service.EntityMapper;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.dto.AnaliseDivergenceDTO;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class AnaliseMapper implements EntityMapper<AnaliseDTO, Analise> {

    private ModelMapper modelMapper = new ModelMapper();;

    @Override
    public Analise toEntity(AnaliseDTO dto) {
        return modelMapper.map(dto, Analise.class);
    }

    @Override
    public AnaliseDTO toDto(Analise entity) {
        AnaliseDTO analiseDto = modelMapper.map(entity, AnaliseDTO.class);
        if(entity.getAnaliseDivergence() != null){
            analiseDto.setAnaliseDivergence(this.convertToAnaliseDivergenceDTO(entity.getAnaliseDivergence()));
        }
        return analiseDto;
    }

    @Override
    public List<Analise> toEntity(List<AnaliseDTO> dtoList) {
        return dtoList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnaliseDTO> toDto(List<Analise> entityList) {
        return entityList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public AnaliseDivergenceDTO convertToAnaliseDivergenceDTO(Analise analise){
        return modelMapper.map(analise, AnaliseDivergenceDTO.class);
    }
}
