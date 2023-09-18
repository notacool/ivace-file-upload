package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "document")
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column
	private Long id;

    @Column(nullable = false, unique = true)
	private String alfrescoId;

    @Column(name = "gustavoId", unique = true)
	private Long gustavoId;

    @Column(name = "ulisesId", unique = true)
	private String ulisesId;

    public Document() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGustavoId() {
        return gustavoId;
    }

    public void setGustavoId(Long gustavoId) {
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