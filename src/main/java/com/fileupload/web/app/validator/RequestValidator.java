package com.fileupload.web.app.validator;

import org.springframework.stereotype.Component;

@Component
public class RequestValidator {

	public boolean validateIDS(String gustavoID, String ulisesID) {
		if (gustavoID == null && ulisesID == null) {
			return false;
		} else if (gustavoID != null && ulisesID != null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean validateRequestCod(String[] metadata) {

		// 0 -> Area
		// 1 -> Anio
		// 2 -> Convocatoria
		// 3 -> X
		// 4 -> Expediente
		// 5 -> Proceso
		// 6 -> Documentacion

		if (metadata[3].equals("Expediente")) {
			if (metadata[4] != null && metadata[5] != null && metadata[6] != null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (metadata[4] == null && metadata[5] == null && metadata[6] != null) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean validateRequestNom(String[] metadata) {

		// 7 -> Area
		// 8 -> Anio
		// 9 -> Convocatoria
		// 10 -> X
		// 11 -> Expediente
		// 12 -> Proceso
		// 13 -> Documentacion

		if (metadata[10].equals("Expediente")) {
			if (metadata[11] != null && metadata[12] != null && metadata[13] != null) {
				return true;
			} else {
				return false;
			}
		} else {
			if (metadata[11] == null && metadata[12] == null && metadata[13] != null) {
				return true;
			} else {
				return false;
			}
		}
	}
}
