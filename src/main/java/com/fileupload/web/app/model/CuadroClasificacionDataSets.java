package com.fileupload.web.app.model;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;
@Component
public class CuadroClasificacionDataSets {
	LinkedHashMap<String, String> mapaAreas;
	LinkedHashMap<String, String> mapaAnios;
	LinkedHashMap<String, String> mapaConvocatorias;
	LinkedHashMap<String, String> mapaExpedientes;
	LinkedHashMap<String, LinkedHashMap<String, String>> mapaProcesosYDocumentaciones;
	LinkedHashMap<String,String> mapaDocumentacionesProcesoSolicitudes; //P01
	LinkedHashMap<String,String> mapaDocumentacionesProcesoPreevaluaciontecnico; //P02
	LinkedHashMap<String,String> mapaDocumentacionesProcesoComisionEvaluacionivace; //P03
	LinkedHashMap<String,String> mapaDocumentacionesProcesoResolucionconcesion; //P04
	LinkedHashMap<String,String> mapaDocumentacionesProcesoComunicacionconcesionabeneficiario; //P05
	LinkedHashMap<String,String> mapaDocumentacionesProcesoAnticipoprestamo; //P06
	LinkedHashMap<String,String> mapaDocumentacionesProcesoEjecuciondeproyecto; //P07
	LinkedHashMap<String,String> mapaDocumentacionesProcesoJustificacionproyecto; //P08
	LinkedHashMap<String,String> mapaDocumentacionesProcesoVerificaciondocumental; //P09
	LinkedHashMap<String,String> mapaDocumentacionesProcesoVerificacionmaterial; //P10
	LinkedHashMap<String,String> mapaDocumentacionesProcesoVerificacionfinal; //P11
	LinkedHashMap<String,String> mapaDocumentacionesProcesoComunicacionserviciopago; //P12
	LinkedHashMap<String,String> mapaDocumentacionesProcesoPagosubvencion; //P13
	LinkedHashMap<String,String> mapaProcesos;
	public CuadroClasificacionDataSets() {
		super();
		initMaps();
	}
	
	private void initMaps() {
		
		//AREAS
		mapaAreas = new LinkedHashMap<>();
		mapaAreas.put("A01", "Energia");
		mapaAreas.put("A02", "Innovacion");
		mapaAreas.put("A03", "Internacional");
		mapaAreas.put("A04", "Parques Empresariales");
		
		//Anios
		mapaAnios = new LinkedHashMap<>();
		mapaAnios.put("2023", "2023");
		
		//Convocatorias
		mapaConvocatorias = new LinkedHashMap<>();
		mapaConvocatorias.put("0001.23", "Primera Convocatoria");
		
		//Expedientes
		mapaExpedientes = new LinkedHashMap<>();
		mapaExpedientes.put("999", "Expediente Primero");
		
		//Procesos + Documentaciones
		mapaDocumentacionesProcesoSolicitudes = new LinkedHashMap<>();
		mapaDocumentacionesProcesoSolicitudes.put("D01", "Solicitud en el registro");
		mapaDocumentacionesProcesoSolicitudes.put("D02", "Documentacion anexa");
		mapaDocumentacionesProcesoSolicitudes.put("D03", "Requerimiento de subsanacion de la solicitud");
		mapaDocumentacionesProcesoSolicitudes.put("D04", "Anexo de requerimiento minimis 2.0");
		mapaDocumentacionesProcesoSolicitudes.put("D05", "Informe propuesta de desistimiento");
		mapaDocumentacionesProcesoSolicitudes.put("D06", "Resolucion de desistimiento");
		mapaDocumentacionesProcesoSolicitudes.put("D07", "Notificación de resolucion de desistimiento");
		mapaDocumentacionesProcesoPreevaluaciontecnico = new LinkedHashMap<>();
		mapaDocumentacionesProcesoPreevaluaciontecnico.put("D01", "Informe técnico de evaluación del técnico y de la comisión");
		mapaDocumentacionesProcesoPreevaluaciontecnico.put("D02", "Propuesta de aprobado");
		mapaDocumentacionesProcesoPreevaluaciontecnico.put("D03", "Propuesta de denegado");
		mapaDocumentacionesProcesoComisionEvaluacionivace = new LinkedHashMap<>();
		mapaDocumentacionesProcesoComisionEvaluacionivace.put("D01", "Listado resumen de la instrucción para la comisión");
		mapaDocumentacionesProcesoComisionEvaluacionivace.put("D02", "Informe del jefe del área");
		mapaDocumentacionesProcesoComisionEvaluacionivace.put("D03", "Acta de la comisión");
		mapaDocumentacionesProcesoComisionEvaluacionivace.put("D04", "Propuesta para resolución (Consellerias)");
		mapaDocumentacionesProcesoComisionEvaluacionivace.put("D05", "Anexo con la lista de expedientes para resolución de concesión");
		mapaDocumentacionesProcesoComisionEvaluacionivace.put("D06", "Anexo con la lista de expedientes para resolución de denegación");
		mapaDocumentacionesProcesoResolucionconcesion = new LinkedHashMap<>();
		mapaDocumentacionesProcesoResolucionconcesion.put("D01", "Firma resolución de la ayuda");
		mapaDocumentacionesProcesoComunicacionconcesionabeneficiario = new LinkedHashMap<>();
		mapaDocumentacionesProcesoComunicacionconcesionabeneficiario.put("D01", "Comunicación de concesión de la ayuda");
		mapaDocumentacionesProcesoComunicacionconcesionabeneficiario.put("D02", "Comunicación de denegación de la ayuda");
		mapaDocumentacionesProcesoComunicacionconcesionabeneficiario.put("D03", "Anexo a la resolución de concesión Documento que establece las condiciones de la ayuda anexo");
		mapaDocumentacionesProcesoComunicacionconcesionabeneficiario.put("D04", "Notificación certificado de deducción fiscal");
		mapaDocumentacionesProcesoComunicacionconcesionabeneficiario.put("D05", "Anexo notificación certificado de deducción fiscal");
		mapaDocumentacionesProcesoAnticipoprestamo = new LinkedHashMap<>();
		mapaDocumentacionesProcesoAnticipoprestamo.put("D01", "Comunicación de concesión definitiva plurianuales");
		mapaDocumentacionesProcesoAnticipoprestamo.put("D02", "Comunicación de concesión condicionada de préstamos");
		mapaDocumentacionesProcesoAnticipoprestamo.put("D03", "Comunicación de concesión préstamos");
		mapaDocumentacionesProcesoAnticipoprestamo.put("D04", "Anexo a la notificación de concesión  para la aceptación de las condiciones del préstamo");
		mapaDocumentacionesProcesoAnticipoprestamo.put("D05", "Anexo a la notificación de concesión Instrucciones de presentación de garantía o aval");
		mapaDocumentacionesProcesoEjecuciondeproyecto = new LinkedHashMap<>();
		mapaDocumentacionesProcesoEjecuciondeproyecto.put("D01", "Solicitud de modificación por parte del beneficiario");
		mapaDocumentacionesProcesoEjecuciondeproyecto.put("D02", "Aprobación de modificación");
		mapaDocumentacionesProcesoEjecuciondeproyecto.put("D03", "Resolución de redistribución de costes");
		mapaDocumentacionesProcesoJustificacionproyecto = new LinkedHashMap<>();
		mapaDocumentacionesProcesoJustificacionproyecto.put("D01", "Presentación de la justificación");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D02", "Documentación anexa");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D03", "Comunicación recordando el plazo de justificación");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D04", "Diligencia de formalización de avales");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D05", "Informe del importe máximo endosable");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D06", "Informe comprobación validez del aval");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D07", "Diligencia de formalización de avales");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D08", "Anexo de incidencias de la verificación en la notificaicón de minoración revocación");
		mapaDocumentacionesProcesoJustificacionproyecto.put("D09", "Informe de propuesta de revocación sin verificación administrativa");
		mapaDocumentacionesProcesoVerificaciondocumental = new LinkedHashMap<>();
		mapaDocumentacionesProcesoVerificaciondocumental.put("D01", "Documentación justificativa");
		mapaDocumentacionesProcesoVerificaciondocumental.put("D02", "Informe técnico de la verificación administrativa del expediente");
		mapaDocumentacionesProcesoVerificaciondocumental.put("D03", "Conformidad");
		mapaDocumentacionesProcesoVerificaciondocumental.put("D04", "Propuesta de fase O para contabilizar");
		mapaDocumentacionesProcesoVerificaciondocumental.put("D05", "Requerimiento de subsanación de la solicitud");
		mapaDocumentacionesProcesoVerificaciondocumental.put("D06", "Notificación resolución de minoración");
		mapaDocumentacionesProcesoVerificaciondocumental.put("D07", "Anexo de incidencias subsanables para el requerimiento de subsanación de la verificacion");
		mapaDocumentacionesProcesoVerificacionmaterial = new LinkedHashMap<>();
		mapaDocumentacionesProcesoVerificacionmaterial.put("D01", "Informe técnico de verificación sobre el terreno del expediente");
		mapaDocumentacionesProcesoVerificacionmaterial.put("D02", "Conformidad");
		mapaDocumentacionesProcesoVerificacionmaterial.put("D03", "Requerimiento de subsanación de la solicitud");
		mapaDocumentacionesProcesoVerificacionmaterial.put("D04", "Notificación resolución de minoración");
		mapaDocumentacionesProcesoVerificacionmaterial.put("D05", "Anexo de incidencias subsanables para el requerimiento de subsanación de la verificacion");
		mapaDocumentacionesProcesoVerificacionfinal = new LinkedHashMap<>();
		mapaDocumentacionesProcesoVerificacionfinal.put("D01", "Informe técnico de la verificación final del expediente");
		mapaDocumentacionesProcesoVerificacionfinal.put("D02", "Anexo con la lista de expedientes para resolución de revocación");
		mapaDocumentacionesProcesoVerificacionfinal.put("D03", "Notificación resolución de revocación");
		mapaDocumentacionesProcesoVerificacionfinal.put("D04", "Informe técnico de propuesta de renuncia");
		mapaDocumentacionesProcesoVerificacionfinal.put("D05", "Anexo que lista los expedientes para la resolución de renuncia");
		mapaDocumentacionesProcesoVerificacionfinal.put("D06", "Resolución de renuncia");
		mapaDocumentacionesProcesoVerificacionfinal.put("D07", "Notificación de resolución de renuncia");
		mapaDocumentacionesProcesoVerificacionfinal.put("D08", "Anexo con la lista de expediente para la resolución de minoración");
		mapaDocumentacionesProcesoVerificacionfinal.put("D09", "Informe técnico de la verificación posterior a la final del expediente");
		mapaDocumentacionesProcesoComunicacionserviciopago = new LinkedHashMap<>();
		mapaDocumentacionesProcesoComunicacionserviciopago.put("D01", "Informe técnico para pago por anticipo");
		mapaDocumentacionesProcesoComunicacionserviciopago.put("D02", "Propuesta de fase K para contabilizar");
		mapaDocumentacionesProcesoComunicacionserviciopago.put("D03", "Propuesta de fase OK para contabilizar");
		mapaDocumentacionesProcesoPagosubvencion = new LinkedHashMap<>();
		mapaDocumentacionesProcesoPagosubvencion.put("D01", "Ratificación de endoso");
		mapaDocumentacionesProcesoPagosubvencion.put("D02", "Comunicación importe de ayuda tras su verificación");
		
		mapaProcesosYDocumentaciones = new LinkedHashMap<>();
		mapaProcesosYDocumentaciones.put("P01",mapaDocumentacionesProcesoSolicitudes);
		mapaProcesosYDocumentaciones.put("P02",mapaDocumentacionesProcesoPreevaluaciontecnico);
		mapaProcesosYDocumentaciones.put("P03",mapaDocumentacionesProcesoComisionEvaluacionivace);
		mapaProcesosYDocumentaciones.put("P04",mapaDocumentacionesProcesoResolucionconcesion);
		mapaProcesosYDocumentaciones.put("P05",mapaDocumentacionesProcesoComunicacionconcesionabeneficiario);
		mapaProcesosYDocumentaciones.put("P06",mapaDocumentacionesProcesoAnticipoprestamo);
		mapaProcesosYDocumentaciones.put("P07",mapaDocumentacionesProcesoEjecuciondeproyecto);
		mapaProcesosYDocumentaciones.put("P08",mapaDocumentacionesProcesoJustificacionproyecto);
		mapaProcesosYDocumentaciones.put("P09",mapaDocumentacionesProcesoVerificaciondocumental);
		mapaProcesosYDocumentaciones.put("P10",mapaDocumentacionesProcesoVerificacionmaterial);
		mapaProcesosYDocumentaciones.put("P11",mapaDocumentacionesProcesoVerificacionfinal);
		mapaProcesosYDocumentaciones.put("P12",mapaDocumentacionesProcesoComunicacionserviciopago);
		mapaProcesosYDocumentaciones.put("P13",mapaDocumentacionesProcesoPagosubvencion);
		
		mapaProcesos = new LinkedHashMap<>();
		mapaProcesos.put("P01", "Solicitudes");
		mapaProcesos.put("P02", "Pre-evaluación técnico");
		mapaProcesos.put("P03", "Comisión evaluación IVACE");
		mapaProcesos.put("P04", "Resolución concesión");
		mapaProcesos.put("P05", "Comunicación concesión a beneficiario");
		mapaProcesos.put("P06", "Anticipo o Préstamo");
		mapaProcesos.put("P07", "Ejecución del proyecto (modificaciones)");
		mapaProcesos.put("P08", "Justificación del proyecto");
		mapaProcesos.put("P09", "Verificación documental");
		mapaProcesos.put("P10", "Verificación material");
		mapaProcesos.put("P11", "Verificación final");
		mapaProcesos.put("P12", "Comunicación al servicio de pago");
		mapaProcesos.put("P13", "Pago subvención");
		
	}

	public LinkedHashMap<String, String> getMapaProcesos() {
		return mapaProcesos;
	}

	public void setMapaProcesos(LinkedHashMap<String, String> mapaProcesos) {
		this.mapaProcesos = mapaProcesos;
	}

	public LinkedHashMap<String, String> getMapaAreas() {
		return mapaAreas;
	}

	public void setMapaAreas(LinkedHashMap<String, String> mapaAreas) {
		this.mapaAreas = mapaAreas;
	}

	public LinkedHashMap<String, String> getMapaAnios() {
		return mapaAnios;
	}

	public void setMapaAnios(LinkedHashMap<String, String> mapaAnios) {
		this.mapaAnios = mapaAnios;
	}

	public LinkedHashMap<String, String> getMapaConvocatorias() {
		return mapaConvocatorias;
	}

	public void setMapaConvocatorias(LinkedHashMap<String, String> mapaConvocatorias) {
		this.mapaConvocatorias = mapaConvocatorias;
	}

	public LinkedHashMap<String, String> getMapaExpedientes() {
		return mapaExpedientes;
	}

	public void setMapaExpedientes(LinkedHashMap<String, String> mapaExpedientes) {
		this.mapaExpedientes = mapaExpedientes;
	}

	public LinkedHashMap<String, LinkedHashMap<String, String>> getMapaProcesosYDocumentaciones() {
		return mapaProcesosYDocumentaciones;
	}

	public void setMapaProcesosYDocumentaciones(
			LinkedHashMap<String, LinkedHashMap<String, String>> mapaProcesosYDocumentaciones) {
		this.mapaProcesosYDocumentaciones = mapaProcesosYDocumentaciones;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoSolicitudes() {
		return mapaDocumentacionesProcesoSolicitudes;
	}

	public void setMapaDocumentacionesProcesoSolicitudes(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoSolicitudes) {
		this.mapaDocumentacionesProcesoSolicitudes = mapaDocumentacionesProcesoSolicitudes;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoPreevaluaciontecnico() {
		return mapaDocumentacionesProcesoPreevaluaciontecnico;
	}

	public void setMapaDocumentacionesProcesoPreevaluaciontecnico(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoPreevaluaciontecnico) {
		this.mapaDocumentacionesProcesoPreevaluaciontecnico = mapaDocumentacionesProcesoPreevaluaciontecnico;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoComisionEvaluacionivace() {
		return mapaDocumentacionesProcesoComisionEvaluacionivace;
	}

	public void setMapaDocumentacionesProcesoComisionEvaluacionivace(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoComisionEvaluacionivace) {
		this.mapaDocumentacionesProcesoComisionEvaluacionivace = mapaDocumentacionesProcesoComisionEvaluacionivace;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoResolucionconcesion() {
		return mapaDocumentacionesProcesoResolucionconcesion;
	}

	public void setMapaDocumentacionesProcesoResolucionconcesion(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoResolucionconcesion) {
		this.mapaDocumentacionesProcesoResolucionconcesion = mapaDocumentacionesProcesoResolucionconcesion;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoComunicacionconcesionabeneficiario() {
		return mapaDocumentacionesProcesoComunicacionconcesionabeneficiario;
	}

	public void setMapaDocumentacionesProcesoComunicacionconcesionabeneficiario(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoComunicacionconcesionabeneficiario) {
		this.mapaDocumentacionesProcesoComunicacionconcesionabeneficiario = mapaDocumentacionesProcesoComunicacionconcesionabeneficiario;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoAnticipoprestamo() {
		return mapaDocumentacionesProcesoAnticipoprestamo;
	}

	public void setMapaDocumentacionesProcesoAnticipoprestamo(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoAnticipoprestamo) {
		this.mapaDocumentacionesProcesoAnticipoprestamo = mapaDocumentacionesProcesoAnticipoprestamo;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoEjecuciondeproyecto() {
		return mapaDocumentacionesProcesoEjecuciondeproyecto;
	}

	public void setMapaDocumentacionesProcesoEjecuciondeproyecto(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoEjecuciondeproyecto) {
		this.mapaDocumentacionesProcesoEjecuciondeproyecto = mapaDocumentacionesProcesoEjecuciondeproyecto;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoJustificacionproyecto() {
		return mapaDocumentacionesProcesoJustificacionproyecto;
	}

	public void setMapaDocumentacionesProcesoJustificacionproyecto(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoJustificacionproyecto) {
		this.mapaDocumentacionesProcesoJustificacionproyecto = mapaDocumentacionesProcesoJustificacionproyecto;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoVerificaciondocumental() {
		return mapaDocumentacionesProcesoVerificaciondocumental;
	}

	public void setMapaDocumentacionesProcesoVerificaciondocumental(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoVerificaciondocumental) {
		this.mapaDocumentacionesProcesoVerificaciondocumental = mapaDocumentacionesProcesoVerificaciondocumental;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoVerificacionmaterial() {
		return mapaDocumentacionesProcesoVerificacionmaterial;
	}

	public void setMapaDocumentacionesProcesoVerificacionmaterial(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoVerificacionmaterial) {
		this.mapaDocumentacionesProcesoVerificacionmaterial = mapaDocumentacionesProcesoVerificacionmaterial;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoVerificacionfinal() {
		return mapaDocumentacionesProcesoVerificacionfinal;
	}

	public void setMapaDocumentacionesProcesoVerificacionfinal(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoVerificacionfinal) {
		this.mapaDocumentacionesProcesoVerificacionfinal = mapaDocumentacionesProcesoVerificacionfinal;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoComunicacionserviciopago() {
		return mapaDocumentacionesProcesoComunicacionserviciopago;
	}

	public void setMapaDocumentacionesProcesoComunicacionserviciopago(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoComunicacionserviciopago) {
		this.mapaDocumentacionesProcesoComunicacionserviciopago = mapaDocumentacionesProcesoComunicacionserviciopago;
	}

	public LinkedHashMap<String, String> getMapaDocumentacionesProcesoPagosubvencion() {
		return mapaDocumentacionesProcesoPagosubvencion;
	}

	public void setMapaDocumentacionesProcesoPagosubvencion(
			LinkedHashMap<String, String> mapaDocumentacionesProcesoPagosubvencion) {
		this.mapaDocumentacionesProcesoPagosubvencion = mapaDocumentacionesProcesoPagosubvencion;
	}



}
