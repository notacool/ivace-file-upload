package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "expediente")
public class Expediente {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigo", unique = true)
	private String codExpediente;

	@Column(nullable = false, name = "nombre")
	private String nomExpediente;

    public Expediente() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodExpediente() {
        return codExpediente;
    }

    public void setCodExpediente(String codExpediente) {
        this.codExpediente = codExpediente;
    }

    public String getNomExpediente() {
        return nomExpediente;
    }

    public void setNomExpediente(String nomExpediente) {
        this.nomExpediente = nomExpediente;
    }
    
}