package com.xxx.b2b.filter;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class LoadProperties {

	private static String devServers = "";

	public boolean isDevServer(String hostName) {
		hostName = hostName.toUpperCase();
		try {
			System.out.println("hostName value :"+hostName);
			System.out.println("devServers value :"+devServers);
			String[] myDevServers = devServers.split("\\s+");
			return Arrays.asList(myDevServers).contains(hostName);
		} catch (Exception exc) {
			return false;
		}
	}

	@Autowired
	public LoadProperties(@Value("${devServers}") String devServers) {
		LoadProperties.devServers = devServers;
	}
}