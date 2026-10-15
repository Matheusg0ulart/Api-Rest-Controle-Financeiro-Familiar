package br.senac.fianceiro.familia.repository;

import br.senac.fianceiro.familia.entity.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamiliaRepository extends JpaRepository<Familia, Long> {

    @Query("SELECT fm.familia FROM FamiliaMembro fm WHERE fm.usuario.id = :usuarioId AND fm.status = 'ATIVO'")
    List<Familia> findFamiliasAtivasPorUsuarioId(@Param("usuarioId") Long usuarioId);
}
