package com.fileupload.web.app.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class GenericRepository {
    @PersistenceContext
    protected EntityManager entityManager;
}