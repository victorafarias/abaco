package br.com.basis.abaco.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.unmodifiableList;


@Service
@Transactional
public class ElasticSearchIndexService {

    private static final Lock reindexLock = new ReentrantLock();
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_WAIT_MS = 2000;

    private final Logger log = LoggerFactory.getLogger(ElasticSearchIndexService.class);
    private final List<Indexador> indexadores;
    private Map<String, Indexador> indexadoresPorCodigo;

    public ElasticSearchIndexService(List<Indexador> indexadores) {
        this.indexadores = unmodifiableList(indexadores);
    }

    public void reindexar(List<String> codigos) {
        if (reindexLock.tryLock()) {
            try {
                codigos.forEach(c -> {
                    Indexador indexador = indexadoresPorCodigo.get(c);
                    if (indexador != null) {
                        reindexarComRetry(indexador);
                    } else {
                        log.warn("Indexador não encontrado para código: {}", c);
                    }
                });
            } finally {
                reindexLock.unlock();
            }
        } else {
            log.warn("Reindexação já em andamento, requisição ignorada.");
        }
    }

    private void reindexarComRetry(Indexador indexador) {
        long waitMs = INITIAL_WAIT_MS;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("Iniciando indexação de {} (tentativa {}/{})", indexador.getCodigo(), attempt, MAX_RETRIES);
                indexador.indexar();
                log.info("Indexação de {} concluída com sucesso.", indexador.getCodigo());
                return;
            } catch (Exception e) {
                log.warn("Tentativa {}/{} falhou para {}: {}", attempt, MAX_RETRIES, indexador.getCodigo(), e.getMessage());
                if (attempt == MAX_RETRIES) {
                    log.error("Todas as {} tentativas falharam para {}. Abortando.", MAX_RETRIES, indexador.getCodigo(), e);
                    throw e;
                }
                try {
                    log.info("Aguardando {}ms antes da próxima tentativa...", waitMs);
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Reindexação interrompida", ie);
                }
                waitMs *= 2;
            }
        }
    }

    @PostConstruct
    public void inicializaIndexadoresPorCodigo() {
        indexadoresPorCodigo = new HashMap<>();
        indexadores.forEach(i -> indexadoresPorCodigo.put(i.getCodigo(), i));
    }
}
