package com.fileupload.web.app.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fileupload.web.app.model.CuadroClasificacionDataSets;

@Component
public class RequestValidator {
	@Autowired
	CuadroClasificacionDataSets cuadroClasificacion;
	public boolean isValidRequest (String[] metadata) {
		
		//0 -> area
		//1 -> anio
		//2 -> convocatoria
		//3 -> X
		//4 -> expediente
		//5 -> proceso
		//6 -> documentacion
		
		if(!cuadroClasificacion.getMapaAreas().containsKey(metadata[0])) {
			return false;
		}
		
		if(!cuadroClasificacion.getMapaAnios().containsKey(metadata[1])) {
			return false;
		}
		if(!cuadroClasificacion.getMapaConvocatorias().containsKey(metadata[2])) {
			return false;
		}
		if(!cuadroClasificacion.getMapaX().containsKey(metadata[3])) {
			return false;
		}
		if(metadata[3] == "Expediente"){
			if(!cuadroClasificacion.getMapaExpedientes().containsKey(metadata[4])) {
				return false;
			}
			if(!cuadroClasificacion.getMapaProcesosYDocumentaciones().containsKey(metadata[5])) {
				return false;
			}
			
			switch(metadata[5]) {
				case "P01" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoSolicitudes().containsKey(metadata[6])) {return false;}
				break;
				case "P02" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoPreevaluaciontecnico().containsKey(metadata[6])){return false;}
				break;
			case "P03" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoComisionEvaluacionivace().containsKey(metadata[6])){return false;}
				break;
			case "P04" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoResolucionconcesion().containsKey(metadata[6])){return false;}
				break;
			case "P05" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionconcesionabeneficiario().containsKey(metadata[6])){return false;}
				break;
			case "P06" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoAnticipoprestamo().containsKey(metadata[6])){return false;}
				break;
			case "P07" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoEjecuciondeproyecto().containsKey(metadata[6])){return false;}
				break;
			case "P08" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoJustificacionproyecto().containsKey(metadata[6])){return false;}
				break;
			case "P09" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoVerificaciondocumental().containsKey(metadata[6])){return false;}
				break;
			case "P10" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionmaterial().containsKey(metadata[6])){return false;}
				break;
			case "P11" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionfinal().containsKey(metadata[6])){return false;}
				break;
			case "P12" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionserviciopago().containsKey(metadata[6])){return false;}
				break;
			case "P13" :
				if(!cuadroClasificacion.getMapaDocumentacionesProcesoPagosubvencion().containsKey(metadata[6])){return false;}
				break;
			}
		}
		if(metadata[3].equals("Normativa")){
			if(!cuadroClasificacion.getMapaDocumentacionesNormativa().containsKey(metadata[6])) {
				return false;
			}
		}

		if(metadata[3].equals("CO Evaluación")){
			if(!cuadroClasificacion.getMapaDocumentacionesCOEvaluacion().containsKey(metadata[6])) {
				return false;
			}
		}
			
		
		return true;
	}
	
}
