package br.senac.fianceiro.meta.repository;

import br.senac.fianceiro.meta.entity.MetaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Long> {

    @Query("""
        SELECT m FROM MetaFinanceira m
        LEFT JOIN FETCH m.titular
        LEFT JOIN FETCH m.familia
        WHERE (m.titular.id = :usuarioId AND m.familia IS NULL)
           OR (m.familia.id IN (
               SELECT fm.familia.id FROM FamiliaMembro fm
               WHERE fm.usuario.id = :usuarioId AND fm.status = 'ATIVO'
           ))
        ORDER BY m.prazo ASC
    """)
    List<MetaFinanceira> listarAcessiveis(@Param("usuarioId") Long usuarioId);
}
