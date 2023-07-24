package com.fileupload.web.app.model;

import javax.persistence.*;

@Entity
@Table(name = "credentials", schema = "alfrescoFileUpload")
public class TCredentials {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private int id;

	@Column(nullable = false, name = "clientID")
	private String clientID;

	@Column(nullable = false, name = "clientPass")
	private String clientPass;

	@Override
	public String toString() {
		return "TCredentials [id=" + id + ", clientID=" + clientID + ", clientPass=" + clientPass + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientPass() {
		return clientPass;
	}

	public void setClientPass(String clientPass) {
		this.clientPass = clientPass;
	}

}