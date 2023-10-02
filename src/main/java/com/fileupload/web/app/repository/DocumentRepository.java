package com.fileupload.web.app.repository;

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fileupload.web.app.model.Document;

@Repository
public class DocumentRepository extends GenericRepository {
    public Document findByID(Long id) {
        TypedQuery<Document> query = entityManager
                .createQuery("from Document d where d.id = :id ", Document.class)
                .setParameter("id", id);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public Document findByGustavoID(String gustavoId) {
        TypedQuery<Document> query = entityManager
                .createQuery("from Document d where d.gustavoId = :gustavoId ", Document.class)
                .setParameter("gustavoId", gustavoId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public Document findByUlisesId(String ulisesId) {
        TypedQuery<Document> query = entityManager
                .createQuery("from Document d where d.ulisesId = :ulisesId ", Document.class)
                .setParameter("ulisesId", ulisesId);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    public List<Document> findAll() {
        return entityManager.createQuery("from Document", Document.class).getResultList();
    }

    public List<Document> getDocumentsByPathId(@Param("pathID") String pathID) {
        TypedQuery<Document> query = entityManager
                .createQuery("from Document d join d.paths dp where dp.paths = :pathID", Document.class)
                .setParameter("pathID", pathID);
        return query.getResultList();
    }

    public void create(Document document) {
        entityManager.persist(document);
    }
}
