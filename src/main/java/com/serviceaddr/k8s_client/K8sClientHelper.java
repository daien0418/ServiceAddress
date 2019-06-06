package com.serviceaddr.k8s_client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class K8sClientHelper {

	private static String innerPort = "8080";

	/**
	 *
	 *
	 * @return 访问容器内部服务的完整地址，由节点IP和容器内部端口映射为的外部端口组成，默认的容器内部端口为8080。
	 *
	 */
	public static String getServiceAddr() throws K8sException{
		String suffix = "http://";
		String serverip = System.getenv("api_server_ip");
		String serverport = System.getenv("api_server_port");

		String url = "";
		String ip = "";
		String port = "";

		if (!StringUtils.isEmpty(serverip) && !StringUtils.isEmpty(serverport)) {
			try {
				url = suffix + serverip + ":" + serverport;
				HttpClientHelper.httpClientGet(url, null, "UTF-8");
			} catch (Exception e) {
				throw new K8sException("K8s server api:" + url + "connect failed!");
			}
		} else {
			serverip = System.getenv("KUBERNETES_SERVICE_HOST");
			serverport = System.getenv("KUBERNETES_SERVICE_PORT");
			if (StringUtils.isEmpty(serverip)) {
				throw new K8sException("Environment variable KUBERNETES_SERVICE_HOST is empty!");
			}
			if (StringUtils.isEmpty(serverport)) {
				throw new K8sException("Environment variable KUBERNETES_SERVICE_PORT is empty!");
			}
			try {
				url = suffix + serverip + ":" + serverport;
				HttpClientHelper.httpClientGet(url, null, "UTF-8");
			} catch (Exception e) {
				throw new K8sException("K8s server api:" + url + "connect failed!");
			}
		}

		String namespace = System.getenv("namespace");
		String service = System.getenv("service");

		if (StringUtils.isEmpty(namespace)) {
			throw new K8sException("Namespace name is empty!");
		}
		if (StringUtils.isEmpty(service)) {
			throw new K8sException("Service name is empty!");
		}

		port = HttpClientHelper.httpClientGet(url + "/api/v1/namespaces/" + namespace + "/services/" + service, null,
				"UTF-8");
		if (port.split("nodePort").length == 1) {
			throw new K8sException("Specified service:" + service + "is not exist!");
		}

		String serviceSelector ="";
		if(port.split("\"selector\":").length==1){
			throw new K8sException("Can not find service selector!");
		}

		serviceSelector = port.split("\"selector\":")[1];
		if(serviceSelector.split("\"app\"").length==1){
			throw new K8sException("There is not \"app\" in service selector!");
		};

		serviceSelector=serviceSelector.split("\"app\"")[1].split(",")[0].split(":")[1].replaceAll("\"", "").replaceAll("}", "").trim();

		if (port.split("\"targetPort\":" + innerPort).length == 1) {
			throw new K8sException("specified innerPort:" + innerPort + "is not exsit!");
		}

		port = port.split("\"targetPort\":" + innerPort)[1].split(":")[1].split("}")[0];

		HashMap<String, String> map = new HashMap<String, String>();
		ip = HttpClientHelper.httpClientGet(
				url + "/api/v1/namespaces/" + namespace + "/pods?labelSelector=app%3D" + serviceSelector, null, "UTF-8");
		if (ip.split("hostIP").length == 1) {
			throw new K8sException("Specified pod:" + service + "is not exist!");
		}
		String[] hostip = ip.split("hostIP");
		String[] podid = ip.split("podIP");

		for (int i = 1; i < hostip.length; i++) {
			map.put(podid[i].substring(3, podid[i].indexOf(",") - 1),
					hostip[i].substring(3, hostip[i].indexOf(",") - 1));
		}
		String hostAddress = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			hostAddress = address.getHostAddress();
			if (StringUtils.isEmpty(hostAddress)) {
				throw new K8sException("Can not get pod flannel ip!");
			}
		} catch (UnknownHostException e) {
			throw new K8sException("Can not get pod flannel ip!");
		}

		return map.get(hostAddress) + ":" + port;
	}

	/**
	 *
	 * @param port
	 *            容器的内部端口
	 * @return 访问容器内部服务的完整地址，由节点IP和容器内部端口映射为的外部端口组成
	 *
	 */
	public static String getServiceAddr (String port) throws K8sException{
		if (StringUtils.isEmpty(port)) {
			throw new K8sException("Specified innerPort:" + port + "is empty!");
		}
		innerPort = port;
		return getServiceAddr();
	}

	/**
	 *
	 * @return 节点IP
	 */
	public static String getServiceIp() throws K8sException{
		String suffix = "http://";
		String serverip = System.getenv("api_server_ip");
		String serverport = System.getenv("api_server_port");
		String url = "";
		String port = "";
		String ip = "";

		if (!StringUtils.isEmpty(serverip) && !StringUtils.isEmpty(serverport)) {
			try {
				url = suffix + serverip + ":" + serverport;
				HttpClientHelper.httpClientGet(url, null, "UTF-8");
			} catch (Exception e) {
				throw new K8sException("K8s server api:" + url + "connect failed!");
			}
		} else {
			serverip = System.getenv("KUBERNETES_SERVICE_HOST");
			serverport = System.getenv("KUBERNETES_SERVICE_PORT");
			if (StringUtils.isEmpty(serverip)) {
				throw new K8sException("Environment variable KUBERNETES_SERVICE_HOST is empty!");
			}
			if (StringUtils.isEmpty(serverport)) {
				throw new K8sException("Environment variable KUBERNETES_SERVICE_PORT is empty!");
			}
			try {
				url = suffix + serverip + ":" + serverport;
				HttpClientHelper.httpClientGet(url, null, "UTF-8");
			} catch (Exception e) {
				throw new K8sException("K8s server api:" + url + "connect failed!");
			}
		}

		String namespace = System.getenv("namespace");
		String service = System.getenv("service");

		if (StringUtils.isEmpty(namespace)) {
			throw new K8sException("Namespace name is empty!");
		}
		if (StringUtils.isEmpty(service)) {
			throw new K8sException("Service name is empty!");
		}

		port = HttpClientHelper.httpClientGet(url + "/api/v1/namespaces/" + namespace + "/services/" + service, null,
				"UTF-8");
		if (port.split("nodePort").length == 1) {
			throw new K8sException("Specified service:" + service + "is not exist!");
		}

		String serviceSelector ="";
		if(port.split("\"selector\":").length==1){
			throw new K8sException("Can not find service selector!");
		}

		serviceSelector = port.split("\"selector\":")[1];
		if(serviceSelector.split("\"app\"").length==1){
			throw new K8sException("There is not \"app\" in service selector!");
		};

		serviceSelector=serviceSelector.split("\"app\"")[1].split(",")[0].split(":")[1].replaceAll("\"", "").replaceAll("}", "").trim();

		HashMap<String, String> map = new HashMap<String, String>();
		ip = HttpClientHelper.httpClientGet(
				url + "/api/v1/namespaces/" + namespace + "/pods?labelSelector=app%3D" + serviceSelector, null, "UTF-8");
		if (ip.split("hostIP").length == 1) {
			throw new K8sException("Specified pod:" + service + " is not exist!");
		}
		String[] hostip = ip.split("hostIP");
		String[] podid = ip.split("podIP");

		for (int i = 1; i < hostip.length; i++) {
			map.put(podid[i].substring(3, podid[i].indexOf(",") - 1),
					hostip[i].substring(3, hostip[i].indexOf(",") - 1));
		}
		String hostAddress = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			hostAddress = address.getHostAddress();
			if (StringUtils.isEmpty(hostAddress)) {
				throw new K8sException("Can not get pod flannel ip!");
			}
		} catch (UnknownHostException e) {
			throw new K8sException("Can not get pod flannel ip!");
		}


		System.out.println(hostAddress);
		Set<String> keyset = map.keySet();
		Iterator<String> iterator = keyset.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			System.out.println("key:"+key+",");
			System.out.println("value:"+map.get(key));
		}

		return map.get(hostAddress);
	}

	/**
	 *
	 *
	 * @return 内部端口映射成的外部端口，默认的容器内部端口为8080
	 */
	public static String getServicePort() throws K8sException{
		String suffix = "http://";
		String serverip = System.getenv("api_server_ip");
		String serverport = System.getenv("api_server_port");
		String url = "";
		String port = "";

		if (!StringUtils.isEmpty(serverip) && !StringUtils.isEmpty(serverport)) {
			try {
				url = suffix + serverip + ":" + serverport;
				HttpClientHelper.httpClientGet(url, null, "UTF-8");
			} catch (Exception e) {
				throw new K8sException("K8s server api:" + url + "connect failed!");
			}
		} else {
			serverip = System.getenv("KUBERNETES_SERVICE_HOST");
			serverport = System.getenv("KUBERNETES_SERVICE_PORT");
			if (StringUtils.isEmpty(serverip)) {
				throw new K8sException("Environment variable KUBERNETES_SERVICE_HOST is empty!");
			}
			if (StringUtils.isEmpty(serverport)) {
				throw new K8sException("Environment variable KUBERNETES_SERVICE_PORT is empty!");
			}
			try {
				url = suffix + serverip + ":" + serverport;
				HttpClientHelper.httpClientGet(url, null, "UTF-8");
			} catch (Exception e) {
				throw new K8sException("K8s server api:" + url + "connect failed!");
			}
		}

		String namespace = System.getenv("namespace");
		String service = System.getenv("service");

		if (StringUtils.isEmpty(namespace)) {
			throw new K8sException("Namespace name is empty!");
		}
		if (StringUtils.isEmpty(service)) {
			throw new K8sException("Service name is empty!");
		}

		port = HttpClientHelper.httpClientGet(url + "/api/v1/namespaces/" + namespace + "/services/" + service, null,
				"UTF-8");
		if (port.split("nodePort").length == 1) {
			throw new K8sException("Specified service:" + service + "is not exist!");
		}

		if (port.split("\"targetPort\":" + innerPort).length == 1) {
			throw new K8sException("Specified innerPort:" + innerPort + "is not exsit!");
		}

		port = port.split("\"targetPort\":" + innerPort)[1].split(":")[1].split("}")[0];

		return port;
	}

	/**
	 *
	 * @param port
	 *            容器的内部端口
	 * @return 内部端口映射成的外部端口
	 */
	public static String getServicePort(String port)throws K8sException {
		if (StringUtils.isEmpty(port)) {
			throw new K8sException("Specified innerPort:" + port + "is empty!");
		}
		innerPort = port;
		return getServicePort();
	}

	public static void main(String args[]) {
		try {
			String result = K8sClientHelper.getServiceIp()+ ":" + K8sClientHelper.getServicePort(args[0]);
			System.out.println(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
