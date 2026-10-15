package br.senac.fianceiro.meta.repository;

import br.senac.fianceiro.meta.entity.AporteMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AporteMetaRepository extends JpaRepository<AporteMeta, Long> {
    List<AporteMeta> findByMetaIdOrderByDataDescCriadoEmDesc(Long metaId);
}
