package br.com.basis.abaco.config;

import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.service.dto.FuncaoTransacaoDTO;
import org.modelmapper.ModelMapper;
// ATUALIZADO: Importação adicionada para configurar o mapeamento
import org.modelmapper.PropertyMap; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        // ATUALIZADO: Mapeamento explícito para a classe FuncaoTransacao.
        // Garante que o campo 'quantidade' seja copiado da Entidade para o DTO (e vice-versa),
        // superando a falha do mapeamento automático.
        
        // Mapeamento: FuncaoTransacao (Entidade) -> FuncaoTransacaoDTO (DTO)
        modelMapper.addMappings(new PropertyMap<FuncaoTransacao, FuncaoTransacaoDTO>() {
            @Override
            protected void configure() {
                map().setQuantidade(source.getQuantidade());
            }
        });

        // Mapeamento reverso: FuncaoTransacaoDTO (DTO) -> FuncaoTransacao (Entidade)
        modelMapper.addMappings(new PropertyMap<FuncaoTransacaoDTO, FuncaoTransacao>() {
            @Override
            protected void configure() {
                map().setQuantidade(source.getQuantidade());
            }
        });

        return modelMapper;
    }
}


//package br.com.basis.abaco.config;
//
//import org.modelmapper.ModelMapper;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ModelMapperConfig {
//
//    @Bean
//    public ModelMapper modelMapper(){
//        return new ModelMapper();
//    }
//}