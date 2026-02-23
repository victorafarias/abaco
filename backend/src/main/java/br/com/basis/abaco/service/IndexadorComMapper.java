package br.com.basis.abaco.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class IndexadorComMapper<A, B, C extends Serializable, D> extends AbstractIndexador {

    private static final int BATCH_SIZE = 100;
    private static final long BATCH_DELAY_MS = 500;

    private  JpaRepository<A, C> jpaRepository;
    private  ElasticsearchRepository<B, C> elasticsearchClassRepository;
    private  EntityMapper<D, A> classMapper;
    private  ElasticsearchTemplate elasticsearchTemplate;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public IndexadorComMapper(ElasticsearchRepository<B, C> elasticsearchClassRepository,
        EntityMapper<D, A> classMapper,
        ElasticsearchTemplate elasticsearchTemplate){
        this.elasticsearchClassRepository = elasticsearchClassRepository;
        this.classMapper = classMapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public void indexar() {
        Class<B> classe = elasticsearchClassRepository.getEntityClass();
        elasticsearchTemplate.deleteIndex(classe);
        try {
            elasticsearchTemplate.createIndex(classe);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        }
        elasticsearchTemplate.putMapping(classe);
        if (jpaRepository != null) {
            if (jpaRepository.count() > 0) {
                int page = 0;
                Page<A> result;
                do {
                    result = jpaRepository.findAll(new PageRequest(page, BATCH_SIZE));
                    List<D> dto = classMapper.toDto(result.getContent());
                    List list = classMapper.toEntity(dto);
                    elasticsearchClassRepository.save(list);
                    log.info("Indexado lote {}/{} de {}", page + 1, result.getTotalPages(), classe.getSimpleName());
                    page++;
                    if (result.hasNext()) {
                        sleep(BATCH_DELAY_MS);
                    }
                } while (result.hasNext());
            }
        } else {
            List<A> all = new ArrayList<>();
            List<D> dto = classMapper.toDto(all);
            List list = classMapper.toEntity(dto);
            elasticsearchClassRepository.save(list);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Indexação interrompida durante pausa entre lotes");
        }
    }

}
