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

  public Path findByCodigos(String codArea, String codAnho, String codConvocatoria, String codX, String codExpediente,
      String codProceso, String codDocumentacion) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codArea = :codArea " + 
                      "and p.codAnho = :codAnho and p.codConvocatoria = :codConvocatoria and p.codX = :codX " + 
                      "and p.codExpediente = :codExpediente and p.codProceso = :codProceso and p.codDocumentacion = :codDocumentacion", Path.class)
        .setParameter("codArea", codArea)
        .setParameter("codAnho", codAnho)
        .setParameter("codConvocatoria", codConvocatoria)
        .setParameter("codX", codX)
        .setParameter("codExpediente", codExpediente)
        .setParameter("codProceso", codProceso)
        .setParameter("codDocumentacion", codDocumentacion);
    return DataAccessUtils.singleResult(query.getResultList());
  }

  public List<Path> findByCodArea(String codArea) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codArea = :codArea ", Path.class)
        .setParameter("codArea", codArea);
    return query.getResultList();
  }

  public List<Path> findByCodAnho(String codAnho) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codAnho = :codAnho ", Path.class)
        .setParameter("codAnho", codAnho);
    return query.getResultList();
  }

  public List<Path> findByCodConvocatoria(String codConvocatoria) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codConvocatoria = :codConvocatoria ", Path.class)
        .setParameter("codConvocatoria", codConvocatoria);
    return query.getResultList();
  }

  public List<Path> findByCodX(String codX) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codX = :codX ", Path.class)
        .setParameter("codX", codX);
    return query.getResultList();
  }

  public List<Path> findByCodExpediente(String codExpediente) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codExpediente = :codExpediente ", Path.class)
        .setParameter("codExpediente", codExpediente);
    return query.getResultList();
  }

  public List<Path> findByCodProceso(String codProceso) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codProceso = :codProceso ", Path.class)
        .setParameter("codProceso", codProceso);
    return query.getResultList();
  }

  public List<Path> findByCodDocumentacion(String codDocumentacion) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.codDocumentacion = :codDocumentacion ", Path.class)
        .setParameter("codDocumentacion", codDocumentacion);
    return query.getResultList();
  }
  
  public Path findByNomPath(String nomPath) {
    TypedQuery<Path> query = entityManager
        .createQuery("from Path p where p.nombrePath = :nomPath ", Path.class)
        .setParameter("nomPath", nomPath);
    return DataAccessUtils.singleResult(query.getResultList());
  }
}
