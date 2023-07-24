package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "documentacion")
public class Documentacion {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigo", unique = true)
	private String codDocumentacion;

	@Column(nullable = false, name = "nombre")
	private String nomDocumentacion;

    public Documentacion() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodDocumentacion() {
        return codDocumentacion;
    }

    public void setCodDocumentacion(String codDocumentacion) {
        this.codDocumentacion = codDocumentacion;
    }

    public String getNomDocumentacion() {
        return nomDocumentacion;
    }

    public void setNomDocumentacion(String nomDocumentacion) {
        this.nomDocumentacion = nomDocumentacion;
    }
    
}