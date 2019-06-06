package com.serviceaddr.k8s_client;

public class K8sException extends Exception{

	private static final long serialVersionUID = 6080334333533082597L;

	public K8sException(String message) {
		super(message);
	}

}
