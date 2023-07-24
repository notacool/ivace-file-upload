package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "anho")
public class Anho {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigo", unique = true)
	private String codAnho;

	@Column(nullable = false, name = "nombre")
	private String nomAnho;

    public Anho() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodAnho() {
        return codAnho;
    }

    public void setCodAnho(String codAnho) {
        this.codAnho = codAnho;
    }

    public String getNomAnho() {
        return nomAnho;
    }

    public void setNomAnho(String nomAnho) {
        this.nomAnho = nomAnho;
    }
    
}