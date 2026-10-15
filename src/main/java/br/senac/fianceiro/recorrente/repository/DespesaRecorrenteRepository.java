package br.senac.fianceiro.recorrente.repository;

import br.senac.fianceiro.recorrente.entity.DespesaRecorrente;
import br.senac.fianceiro.recorrente.entity.StatusRecorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DespesaRecorrenteRepository extends JpaRepository<DespesaRecorrente, Long> {

    @Query("SELECT dr FROM DespesaRecorrente dr WHERE dr.conta.id IN :contasIds ORDER BY dr.diaVencimento ASC")
    List<DespesaRecorrente> findByContaIdIn(@Param("contasIds") List<Long> contasIds);

    @Query("SELECT dr FROM DespesaRecorrente dr WHERE dr.status = :status AND dr.proximaGeracao <= :dataLimite")
    List<DespesaRecorrente> findAtivasParaGeracao(@Param("status") StatusRecorrencia status,
                                                   @Param("dataLimite") LocalDate dataLimite);
}
