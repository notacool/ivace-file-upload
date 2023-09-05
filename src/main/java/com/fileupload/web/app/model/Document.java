package com.fileupload.web.app.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "document")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

    @Column(nullable = false, name = "alfrescoId")
	private String alfrescoId;

    @Column(name = "gustavoId")
	private String gustavoId;

    @Column(name = "ulisesId")
	private String ulisesId;

    public Document() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGustavoId() {
        return gustavoId;
    }

    public void setGustavoId(String gustavoId) {
        this.gustavoId = gustavoId;
    }

    public String getUlisesId() {
        return ulisesId;
    }

    public void setUlisesId(String ulisesId) {
        this.ulisesId = ulisesId;
    }

    public String getAlfrescoId() {
        return alfrescoId;
    }

    public void setAlfrescoId(String alfrescoId) {
        this.alfrescoId = alfrescoId;
    }

}