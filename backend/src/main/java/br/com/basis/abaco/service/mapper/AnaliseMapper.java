package br.com.basis.abaco.service.mapper;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.service.EntityMapper;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.dto.AnaliseDivergenceDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.List;
import java.util.stream.Collectors;

public class AnaliseMapper implements EntityMapper<AnaliseDTO, Analise> {

    private ModelMapper modelMapper = new ModelMapper();

    public AnaliseMapper(){
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.createTypeMap(Analise.class, AnaliseDTO.class)
            .addMappings(new PropertyMap<Analise, AnaliseDTO>() {
                @Override
                protected void configure() {
                    skip(destination.getFuncaoDados());
                    skip(destination.getFuncaoTransacaos());
                }
            });
    }


    @Override
    public Analise toEntity(AnaliseDTO dto) {
        return modelMapper.map(dto, Analise.class);
    }

    @Override
    public AnaliseDTO toDto(Analise entity) {
        AnaliseDTO analiseDto = modelMapper.map(entity, AnaliseDTO.class);
        if(entity.getAnaliseDivergence() != null){
            analiseDto.setAnaliseDivergence(modelMapper.map(entity.getAnaliseDivergence(), AnaliseDivergenceDTO.class));
        }
        if(entity.getPfTotal() != null){
            analiseDto.setPfTotalValor(entity.getPfTotal().doubleValue());
        }
        if(entity.getAdjustPFTotal() != null){
            analiseDto.setPfTotalAjustadoValor(entity.getAdjustPFTotal().doubleValue());
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

}
