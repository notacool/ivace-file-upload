package com.fileupload.web.app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fileupload.web.app.model.CuadroClasificacionDataSets;
import com.fileupload.web.app.repository.PathRepository;

@Component
public class RequestValidator {
	@Autowired
	CuadroClasificacionDataSets cuadroClasificacion;

	@Autowired
	PathRepository pathRepository;

	public boolean isValidRequest(String nomPath) {

		// 0 -> area
		// 1 -> anio
		// 2 -> convocatoria
		// 3 -> X
		// 4 -> expediente
		// 5 -> proceso
		// 6 -> documentacion

		if(pathRepository.findByCodArea(nomPath) == null){
			return false;
		} else {
			return true;
		}
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
