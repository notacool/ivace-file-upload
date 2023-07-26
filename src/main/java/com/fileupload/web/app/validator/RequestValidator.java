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
}
