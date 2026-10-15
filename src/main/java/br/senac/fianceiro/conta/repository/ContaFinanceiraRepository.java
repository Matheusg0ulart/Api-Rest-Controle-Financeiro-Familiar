package br.senac.fianceiro.conta.repository;

import br.senac.fianceiro.conta.entity.ContaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, Long> {

    List<ContaFinanceira> findByTitularIdAndAtivaTrue(Long titularId);

    @Query("SELECT c FROM ContaFinanceira c " +
            "WHERE c.ativa = true AND (" +
            "   c.titular.id = :usuarioId OR " +
            "   c.familia.id IN (SELECT fm.familia.id FROM FamiliaMembro fm WHERE fm.usuario.id = :usuarioId AND fm.status = 'ATIVO')" +
            ") ORDER BY c.nome ASC")
    List<ContaFinanceira> findContasAcessiveis(@Param("usuarioId") Long usuarioId);
}
