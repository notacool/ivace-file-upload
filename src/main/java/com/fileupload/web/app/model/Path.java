package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "path")
public class Path {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "codigoArea")
	private String codArea;

	@Column(nullable = false, name = "nombreArea")
	private String nomArea;

	@Column(nullable = false, name = "tituloArea")
	private String tituloArea;

	@Column(nullable = false, name = "descripcionArea")
	private String descripcionArea;

	@Column(nullable = false, name = "codigoAnho")
	private String codAnho;

	@Column(nullable = false, name = "nombreAnho")
	private String nomAnho;

	@Column(nullable = false, name = "tituloAnho")
	private String tituloAnho;

	@Column(nullable = false, name = "descripcionAnho")
	private String descripcionAnho;

	@Column(nullable = false, name = "codigoConvocatoria")
	private String codConvocatoria;

	@Column(nullable = false, name = "nombreConvocatoria")
	private String nomConvocatoria;

	@Column(nullable = false, name = "tituloConvocatoria")
	private String tituloConvocatoria;

	@Column(nullable = false, name = "descripcionConvocatoria")
	private String descripcionConvocatoria;

	@Column(nullable = false, name = "codigoExpediente")
	private String codExpediente;

	@Column(nullable = false, name = "nombreExpediente")
	private String nomExpediente;

	@Column(nullable = false, name = "tituloExpediente")
	private String tituloExpediente;

	@Column(nullable = false, name = "descripcionExpediente")
	private String descripcionExpediente;

	@Column(nullable = false, name = "codigoProceso")
	private String codProceso;

	@Column(nullable = false, name = "nombreProceso")
	private String nomProceso;

	@Column(nullable = false, name = "tituloProceso")
	private String tituloProceso;

	@Column(nullable = false, name = "descripcionProceso")
	private String descripcionProceso;

	@Column(nullable = false, name = "codigoDocumentacion") 
	private String codDocumentacion;

	@Column(nullable = false, name = "nombreDocumentacion")
	private String nomDocumentacion;

	@Column(nullable = false, name = "tituloDocumentacion")
	private String tituloDocumentacion;

	@Column(nullable = false, name = "descripcionDocumentacion")
	private String descripcionDocumentacion;

    @Column(nullable = false, name = "nombrePath")
	private String nomPath;

    @Column(nullable = false, name = "codigoPath")
	private String codPath;

    public Path() {
    }
    
    public Path(String codArea, String nomArea, String codAnho, String nomAnho, String codConvocatoria,
            String nomConvocatoria, String codExpediente, String nomExpediente, String codProceso, String nomProceso,
            String codDocumentacion, String nomDocumentacion) {
        this.codArea = codArea;
        this.nomArea = nomArea;
        this.codAnho = codAnho;
        this.nomAnho = nomAnho;
        this.codConvocatoria = codConvocatoria;
        this.nomConvocatoria = nomConvocatoria;
        this.codExpediente = codExpediente;
        this.nomExpediente = nomExpediente;
        this.codProceso = codProceso;
        this.nomProceso = nomProceso;
        this.codDocumentacion = codDocumentacion;
        this.nomDocumentacion = nomDocumentacion;
        this.descripcionArea = nomArea;
        this.tituloArea = codArea;
        this.descripcionAnho = nomArea + " " + nomAnho;
        this.tituloAnho = codArea + " " + codAnho;
        this.descripcionConvocatoria = nomArea + " " + nomAnho + " " + nomConvocatoria;
        this.tituloConvocatoria = codArea + " " + codAnho + " " + codConvocatoria;
        this.descripcionExpediente = nomArea + " " + nomAnho + " " + nomConvocatoria + " " + nomExpediente;
        this.tituloExpediente = codArea + " " + codAnho + " " + codConvocatoria + " " + codExpediente;
        this.descripcionProceso = nomArea + " " + nomAnho + " " + nomConvocatoria + " " + nomExpediente + " " + nomProceso;
        this.tituloProceso = codArea + " " + codAnho + " " + codConvocatoria + " " + codExpediente + " " + codProceso;
        this.descripcionDocumentacion = nomArea + " " + nomAnho + " " + nomConvocatoria + " " + nomExpediente + " " + nomProceso + " " + nomDocumentacion;
        this.tituloProceso = codArea + " " + codAnho + " " + codConvocatoria + " " + codExpediente + " " + codProceso + " " + codDocumentacion;
        this.codPath = "Sites/ivace/documentLibrary/" + codArea + "/" + codAnho + "/" + codConvocatoria + "/" + codExpediente + "/" + codProceso + "/" + codDocumentacion;
        this.nomPath = "Sites/ivace/documentLibrary/" + nomArea + "/" + nomAnho + "/" + nomConvocatoria + "/" + nomExpediente + "/" + nomProceso + "/" + nomDocumentacion;
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

    public String getTituloArea() {
        return tituloArea;
    }

    public void setTituloArea(String tituloArea) {
        this.tituloArea = tituloArea;
    }

    public String getDescripcionArea() {
        return descripcionArea;
    }

    public void setDescripcionArea(String descripcionArea) {
        this.descripcionArea = descripcionArea;
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

    public String getTituloAnho() {
        return tituloAnho;
    }

    public void setTituloAnho(String tituloAnho) {
        this.tituloAnho = tituloAnho;
    }

    public String getDescripcionAnho() {
        return descripcionAnho;
    }

    public void setDescripcionAnho(String descripcionAnho) {
        this.descripcionAnho = descripcionAnho;
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

    public String getTituloConvocatoria() {
        return tituloConvocatoria;
    }

    public void setTituloConvocatoria(String tituloConvocatoria) {
        this.tituloConvocatoria = tituloConvocatoria;
    }

    public String getDescripcionConvocatoria() {
        return descripcionConvocatoria;
    }

    public void setDescripcionConvocatoria(String descripcionConvocatoria) {
        this.descripcionConvocatoria = descripcionConvocatoria;
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

    public String getTituloExpediente() {
        return tituloExpediente;
    }

    public void setTituloExpediente(String tituloExpediente) {
        this.tituloExpediente = tituloExpediente;
    }

    public String getDescripcionExpediente() {
        return descripcionExpediente;
    }

    public void setDescripcionExpediente(String descripcionExpediente) {
        this.descripcionExpediente = descripcionExpediente;
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

    public String getTituloProceso() {
        return tituloProceso;
    }

    public void setTituloProceso(String tituloProceso) {
        this.tituloProceso = tituloProceso;
    }

    public String getDescripcionProceso() {
        return descripcionProceso;
    }

    public void setDescripcionProceso(String descripcionProceso) {
        this.descripcionProceso = descripcionProceso;
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

    public String getTituloDocumentacion() {
        return tituloDocumentacion;
    }

    public void setTituloDocumentacion(String tituloDocumentacion) {
        this.tituloDocumentacion = tituloDocumentacion;
    }

    public String getDescripcionDocumentacion() {
        return descripcionDocumentacion;
    }

    public void setDescripcionDocumentacion(String descripcionDocumentacion) {
        this.descripcionDocumentacion = descripcionDocumentacion;
    }

    public String getNomPath() {
        return nomPath;
    }

    public void setNomPath(String nomPath) {
        this.nomPath = nomPath;
    }

    public String getCodPath() {
        return codPath;
    }

    public void setCodPath(String codPath) {
        this.codPath = codPath;
    }
    
}