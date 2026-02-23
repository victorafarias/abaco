package br.com.basis.abaco.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;

public class IndexadorSemMapper<A, B extends Serializable> extends AbstractIndexador {

    private static final int BATCH_SIZE = 100;
    private static final long BATCH_DELAY_MS = 500;

    private final JpaRepository<A, B> jpaRepository;
    private final ElasticsearchRepository<A, B> elasticsearchClassRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public IndexadorSemMapper(JpaRepository<A, B> jpaRepository, ElasticsearchRepository<A, B> elasticsearchClassRepository, ElasticsearchTemplate elasticsearchTemplate) {
        this.jpaRepository = jpaRepository;
        this.elasticsearchClassRepository = elasticsearchClassRepository;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public void indexar() {
        Class<A> classe = elasticsearchClassRepository.getEntityClass();
        elasticsearchTemplate.deleteIndex(classe);
        try {
            elasticsearchTemplate.createIndex(classe);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
        }
        elasticsearchTemplate.putMapping(classe);
        if (jpaRepository.count() > 0) {
            int page = 0;
            Page<A> result;
            do {
                result = jpaRepository.findAll(new PageRequest(page, BATCH_SIZE));
                elasticsearchClassRepository.save(result.getContent());
                log.info("Indexado lote {}/{} de {}", page + 1, result.getTotalPages(), classe.getSimpleName());
                page++;
                if (result.hasNext()) {
                    sleep(BATCH_DELAY_MS);
                }
            } while (result.hasNext());
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
