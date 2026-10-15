package br.senac.fianceiro.orcamento.repository;

import br.senac.fianceiro.orcamento.entity.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {

    @Query("""
        SELECT o FROM Orcamento o
        LEFT JOIN FETCH o.categoria
        LEFT JOIN FETCH o.titular
        LEFT JOIN FETCH o.familia
        WHERE (o.titular.id = :usuarioId AND o.familia IS NULL)
           OR (o.familia.id IN (
               SELECT fm.familia.id FROM FamiliaMembro fm 
               WHERE fm.usuario.id = :usuarioId AND fm.status = 'ATIVO'
           ))
        ORDER BY o.id DESC
    """)
    List<Orcamento> listarAcessiveis(@Param("usuarioId") Long usuarioId);
}
