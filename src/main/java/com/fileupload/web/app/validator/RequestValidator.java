package com.fileupload.web.app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fileupload.web.app.model.CuadroClasificacionDataSets;

@Component
public class RequestValidator {
	@Autowired
	CuadroClasificacionDataSets cuadroClasificacion;

	public boolean isValidRequest(String[] metadata) {

		// 0 -> area
		// 1 -> anio
		// 2 -> convocatoria
		// 3 -> expediente
		// 4 -> proceso
		// 5 -> documentacion

		if (!cuadroClasificacion.getMapaAreas().containsKey(metadata[0])) {
			return false;
		}

		if (!cuadroClasificacion.getMapaAnios().containsKey(metadata[1])) {
			return false;
		}
		if (!cuadroClasificacion.getMapaConvocatorias().containsKey(metadata[2])) {
			return false;
		}
		if (!cuadroClasificacion.getMapaExpedientes().containsKey(metadata[3])) {
			return false;
		}
		if (!cuadroClasificacion.getMapaProcesosYDocumentaciones().containsKey(metadata[4])) {
			return false;
		}

		switch (metadata[4]) {
			case "P01":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoSolicitudes().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P02":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoPreevaluaciontecnico().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P03":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoComisionEvaluacionivace()
						.containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P04":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoResolucionconcesion().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P05":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionconcesionabeneficiario()
						.containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P06":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoAnticipoprestamo().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P07":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoEjecuciondeproyecto().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P08":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoJustificacionproyecto()
						.containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P09":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoVerificaciondocumental()
						.containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P10":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionmaterial().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P11":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionfinal().containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P12":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionserviciopago()
						.containsKey(metadata[5])) {
					return false;
				}
				break;
			case "P13":
				if (!cuadroClasificacion.getMapaDocumentacionesProcesoPagosubvencion().containsKey(metadata[5])) {
					return false;
				}
				break;
		}

		return true;
	}

	public boolean isValidDocumentId(String id) {

		if (!id.substring(0, 3).equals("ES_")) {
			return false;
		} else if (!id.substring(3, 5).matches("[a-zA-Z]+")) {
			return false;
		} else if (!isNumeric(id.substring(5, 12))) {
			return false;
		} else if (id.charAt(12) != '_') {
			return false;
		} else if (!isNumeric(id.substring(13, 17))) {
			return false;
		} else if (id.charAt(17) != '_') {
			return false;
		} else if (id.length() > 52) {
			return false;
		} else {
			return true;
		}

	}

	public boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
