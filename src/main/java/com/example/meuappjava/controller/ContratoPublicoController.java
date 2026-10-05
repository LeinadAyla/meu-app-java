package com.example.meuappjava.controller;

import com.example.meuappjava.audit.Auditado;
import com.example.meuappjava.domain.dto.ContratoPublicoRequest;
import com.example.meuappjava.domain.dto.ContratoPublicoResponse;
import com.example.meuappjava.domain.dto.DashboardSummaryResponse;
import com.example.meuappjava.service.ContratoPublicoService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/contratos")
@Tag(name = "Contratos públicos", description = "Ingestão e consulta de contratos públicos.")
@SecurityRequirement(name = "bearerAuth")
public class ContratoPublicoController {

    private final ContratoPublicoService service;

    public ContratoPublicoController(ContratoPublicoService service) {
        this.service = service;
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
    public Page<ContratoPublicoResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return service.listar(pageable);
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
