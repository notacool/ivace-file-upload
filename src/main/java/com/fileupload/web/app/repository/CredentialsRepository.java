package com.fileupload.web.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fileupload.web.app.model.TCredentials;

@Repository
public interface CredentialsRepository extends CrudRepository<TCredentials, Integer> {

	@Query("Select tc from TCredentials tc where tc.clientID = :clientID and tc.clientPass = :clientPass")
	TCredentials checkCredentials(@Param("clientID") String clientID, @Param("clientPass") String clientPass);
	
}
