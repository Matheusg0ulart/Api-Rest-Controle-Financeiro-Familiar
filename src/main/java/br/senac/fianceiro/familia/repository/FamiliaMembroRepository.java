package br.senac.fianceiro.familia.repository;

import br.senac.fianceiro.familia.entity.FamiliaMembro;
import br.senac.fianceiro.familia.entity.StatusMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamiliaMembroRepository extends JpaRepository<FamiliaMembro, Long> {

    Optional<FamiliaMembro> findByFamiliaIdAndUsuarioId(Long familiaId, Long usuarioId);

    Optional<FamiliaMembro> findByFamiliaIdAndUsuarioIdAndStatus(Long familiaId, Long usuarioId, StatusMembro status);

    @Query("SELECT fm FROM FamiliaMembro fm JOIN FETCH fm.usuario WHERE fm.familia.id = :familiaId")
    List<FamiliaMembro> findByFamiliaIdWithUsuario(@Param("familiaId") Long familiaId);

    boolean existsByFamiliaIdAndUsuarioId(Long familiaId, Long usuarioId);
}
