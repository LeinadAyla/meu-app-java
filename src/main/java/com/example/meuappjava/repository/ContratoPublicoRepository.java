package com.example.meuappjava.repository;

import com.example.meuappjava.domain.ContratoPublico;
import com.example.meuappjava.domain.enums.NivelRisco;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratoPublicoRepository extends JpaRepository<ContratoPublico, UUID> {

    long countByNivelRiscoIn(Collection<NivelRisco> niveis);

    @Query("select coalesce(sum(c.valorContratado), 0) from ContratoPublico c")
    BigDecimal totalValorContratado();

    @Query("select avg(c.valorContratado) from ContratoPublico c where c.categoria = :categoria")
    Optional<Double> mediaValorPorCategoria(@Param("categoria") String categoria);

    @Query("""
            select c.categoria as categoria, count(c) as quantidade
            from ContratoPublico c
            where c.nivelRisco <> :nivelBaixo
            group by c.categoria
            order by count(c) desc
            """)
    List<CategoriaAnomaliasProjection> contarAnomaliasPorCategoria(
            @Param("nivelBaixo") NivelRisco nivelBaixo);
}
