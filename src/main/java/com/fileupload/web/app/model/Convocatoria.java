package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "convocatoria")
public class Convocatoria {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigo", unique = true)
	private String codConvocatoria;

	@Column(nullable = false, name = "nombre")
	private String nomConvocatoria;

    public Convocatoria() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodConvocatoria() {
        return codConvocatoria;
    }

    public void setCodConvocatoria(String codConvocatoria) {
        this.codConvocatoria = codConvocatoria;
    }

    public String getNomConvocatoria() {
        return nomConvocatoria;
    }

    public void setNomConvocatoria(String nomConvocatoria) {
        this.nomConvocatoria = nomConvocatoria;
    }
    
}