package br.senac.fianceiro.transacao.repository;

import br.senac.fianceiro.transacao.entity.TipoTransacao;
import br.senac.fianceiro.transacao.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {

    @Query("SELECT t FROM Transacao t WHERE " +
            "(t.conta.id IN :contasIds OR (t.contaDestino IS NOT NULL AND t.contaDestino.id IN :contasIds)) " +
            "AND (:inicio IS NULL OR t.dataCompetencia >= :inicio) " +
            "AND (:fim IS NULL OR t.dataCompetencia <= :fim) " +
            "AND (:tipo IS NULL OR t.tipo = :tipo) " +
            "AND (:contaId IS NULL OR t.conta.id = :contaId OR t.contaDestino.id = :contaId) " +
            "AND (:categoriaId IS NULL OR t.categoria.id = :categoriaId) " +
            "ORDER BY t.dataCompetencia DESC, t.id DESC")
    List<Transacao> findComFiltros(
            @Param("contasIds") List<Long> contasIds,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("tipo") TipoTransacao tipo,
            @Param("contaId") Long contaId,
            @Param("categoriaId") Long categoriaId
    );
}
