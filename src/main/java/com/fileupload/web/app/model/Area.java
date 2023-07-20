package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "area")
public class Area {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigo", unique = true)
	private String codArea;

	@Column(nullable = false, name = "nombre")
	private String nomArea;

    public Area() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodArea() {
        return codArea;
    }

    public void setCodArea(String codArea) {
        this.codArea = codArea;
    }

    public String getNomArea() {
        return nomArea;
    }

    public void setNomArea(String nomArea) {
        this.nomArea = nomArea;
    }
    
}