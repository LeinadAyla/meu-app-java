package com.example.meuappjava.service;

import com.example.meuappjava.domain.ContratoPublico;
import com.example.meuappjava.domain.ResultadoAnomalia;
import com.example.meuappjava.domain.dto.ContratoPublicoRequest;
import com.example.meuappjava.domain.dto.ContratoPublicoResponse;
import com.example.meuappjava.domain.event.ContratoSuspeitoRegistrado;
import com.example.meuappjava.repository.ContratoPublicoRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratoPublicoService {

    private final ContratoPublicoRepository repository;
    private final AnomaliaService anomaliaService;
    private final ApplicationEventPublisher eventPublisher;

    public ContratoPublicoService(
            ContratoPublicoRepository repository,
            AnomaliaService anomaliaService,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.anomaliaService = anomaliaService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ContratoPublicoResponse criar(ContratoPublicoRequest request) {
        ContratoPublico contrato = new ContratoPublico(
                request.numeroProcesso(),
                request.numeroContrato(),
                request.objeto(),
                request.orgaoContratante(),
                request.cnpjContratado(),
                request.nomeContratado(),
                request.categoria(),
                request.valorOrcamento(),
                request.valorContratado(),
                request.tipoContratacao(),
                request.dataAssinatura(),
                request.inicioVigencia(),
                request.fimVigencia());
        ResultadoAnomalia resultado = anomaliaService.analisar(contrato);
        contrato.registrarRisco(resultado.nivelRisco(), resultado.detalhes());
        ContratoPublico salvo = repository.save(contrato);
        if (resultado.suspeito()) {
            eventPublisher.publishEvent(new ContratoSuspeitoRegistrado(salvo.getId(), resultado.nivelRisco()));
        }
        return ContratoPublicoResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ContratoPublicoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ContratoPublicoResponse::from);
    }

    @Transactional(readOnly = true)
    public Optional<ContratoPublicoResponse> buscarPorId(UUID id) {
        return repository.findById(id)
                .map(ContratoPublicoResponse::from);
    }
}
