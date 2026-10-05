package com.example.meuappjava.repository;

import com.example.meuappjava.domain.ContratoPublico;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface ContratoPublicoRepository extends JpaRepository<ContratoPublico, UUID> {

    @Query("select avg(c.valorContratado) from ContratoPublico c where c.categoria = :categoria")
    Optional<Double> mediaValorPorCategoria(@Param("categoria") String categoria);
}
