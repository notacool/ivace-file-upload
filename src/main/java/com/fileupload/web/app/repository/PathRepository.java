package com.fileupload.web.app.repository;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import com.fileupload.web.app.model.Path;

@Repository
public class PathRepository extends GenericRepository {

  public void create(Path path) {
    entityManager.persist(path);
  }

  public List<Path> findAll() {
    return entityManager.createQuery("from Path", Path.class).getResultList();
  }

  public Path findByCodigos(String codArea, String codAnho, String codConvocatoria, String codExpediente,
      String codProceso, String codDocumentacion) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codArea = :codArea " + 
                      "and p.codAnho = :codAnho and p.codConvocatoria = :codConvocatoria " + 
                      "and p.codExpediente = :codExpediente and p.codProceso = :codProceso and p.codDocumentacion = :codDocumentacion", Path.class)
        .setParameter("codArea", codArea)
        .setParameter("codAnho", codAnho)
        .setParameter("codConvocatoria", codConvocatoria)
        .setParameter("codExpediente", codExpediente)
        .setParameter("codProceso", codProceso)
        .setParameter("codDocumentacion", codDocumentacion);
    return DataAccessUtils.singleResult(query.getResultList());
  }
  
  public Path findByNomPath(String nomPath) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.nombrePath = :nomPath ", Path.class)
        .setParameter("nomPath", nomPath);
    return DataAccessUtils.singleResult(query.getResultList());
  }
}
