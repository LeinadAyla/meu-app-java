package com.example.meuappjava.controller;

import com.example.meuappjava.audit.Auditado;
import com.example.meuappjava.domain.dto.ContratoPublicoRequest;
import com.example.meuappjava.domain.dto.ContratoPublicoResponse;
import com.example.meuappjava.domain.dto.DashboardSummaryResponse;
import com.example.meuappjava.domain.dto.SimulacaoRiscoRequest;
import com.example.meuappjava.domain.dto.SimulacaoRiscoResponse;
import com.example.meuappjava.domain.enums.NivelRisco;
import com.example.meuappjava.service.ContratoPublicoService;
import com.example.meuappjava.service.MakeWebhookService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/contratos")
@Tag(name = "Contratos públicos", description = "Ingestão e consulta de contratos públicos.")
@SecurityRequirement(name = "bearerAuth")
public class ContratoPublicoController {

    private final ContratoPublicoService service;
    private final MakeWebhookService makeWebhookService;

    public ContratoPublicoController(ContratoPublicoService service, MakeWebhookService makeWebhookService) {
        this.service = service;
        this.makeWebhookService = makeWebhookService;
    }

    @PostMapping
    @Auditado("CONTRATO_CRIADO")
    @PreAuthorize("hasAnyRole('INGESTOR', 'ADMIN')")
    @Operation(summary = "Ingerir contrato público", description = "Valida e registra o contrato, calculando seu risco.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrato criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public ResponseEntity<ContratoPublicoResponse> criar(@Valid @RequestBody ContratoPublicoRequest request) {
        ContratoPublicoResponse response = service.criar(request);
        makeWebhookService.enviarAlerta(
                response.id().toString(),
                response.orgaoContratante(),
                response.nomeContratado(),
                response.valorContratado(),
                response.nivelRisco(),
                response.detalhesAnomalia());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Listar contratos", description = "Lista contratos de forma paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de contratos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public Page<ContratoPublicoResponse> listar(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) NivelRisco nivelRisco,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "") String busca) {
        return service.listar(pageable, nivelRisco, categoria, busca);
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Listar categorias de contratos")
    public List<String> listarCategorias() {
        return service.listarCategorias();
    }

    @GetMapping("/resumo")
    @Auditado("RESUMO_CONTRATOS_CONSULTADO")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Consultar indicadores do dashboard",
            description = "Retorna totais auditados e distribuição de anomalias por categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores do dashboard"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public DashboardSummaryResponse resumo() {
        return service.resumo();
    }

    @PostMapping("/simulacao-risco")
    @PreAuthorize("hasAnyRole('INGESTOR', 'AUDITOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Simular risco antes da ingestão",
            description = "Gera uma estimativa informativa sem persistir o contrato.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulação calculada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão")
    })
    public SimulacaoRiscoResponse simular(@Valid @RequestBody SimulacaoRiscoRequest request) {
        SimulacaoRiscoResponse response = service.simular(request);
        makeWebhookService.enviarAlerta(
                "SIM-" + UUID.randomUUID(),
                "Simulação",
                response.fornecedor(),
                request.valor(),
                response.nivelRisco(),
                String.join("; ", response.sinais()));
        return response;
    }

    @PostMapping("/{id}/publicar-facebook")
    @Auditado("CONTRATO_ENVIADO_PARA_PUBLICACAO")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Enviar contrato ao fluxo de publicação no Facebook",
            description = "Dispara o webhook do Make sob demanda para um contrato existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publicação enviada ao webhook"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão"),
            @ApiResponse(responseCode = "404", description = "Contrato não encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha ao enviar ao webhook")
    })
    public ResponseEntity<Void> publicarNoFacebook(@PathVariable UUID id) {
        ContratoPublicoResponse contrato = service.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado."));
        makeWebhookService.publicarContrato(
                contrato.id().toString(),
                contrato.numeroProcesso(),
                contrato.orgaoContratante(),
                contrato.nomeContratado(),
                contrato.valorContratado(),
                contrato.nivelRisco(),
                contrato.detalhesAnomalia());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Auditado("CONTRATO_CONSULTADO")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ANALISTA', 'ADMIN')")
    @Operation(summary = "Consultar contrato", description = "Busca um contrato pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrato encontrado"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão"),
            @ApiResponse(responseCode = "404", description = "Contrato não encontrado")
    })
    public ResponseEntity<ContratoPublicoResponse> buscarPorId(@PathVariable UUID id) {
        Optional<ContratoPublicoResponse> response = service.buscarPorId(id);
        return ResponseEntity.of(response);
    }
}
