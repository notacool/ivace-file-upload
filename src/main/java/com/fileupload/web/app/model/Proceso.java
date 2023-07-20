package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "proceso")
public class Proceso {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigo", unique = true)
	private String codProceso;

	@Column(nullable = false, name = "nombre")
	private String nomProceso;

    public Proceso() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodProceso() {
        return codProceso;
    }

    public void setCodProceso(String codProceso) {
        this.codProceso = codProceso;
    }

    public String getNomProceso() {
        return nomProceso;
    }

    public void setNomProceso(String nomProceso) {
        this.nomProceso = nomProceso;
    }
    
}