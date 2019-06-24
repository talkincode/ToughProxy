package org.toughproxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public class SocksProxyClientTest {

//	@Test
	public void testHttp() throws Exception {
		final String user = "test01";
		final String password = "888888";
		
		Proxy proxyTest = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("192.168.88.198", 1808));

		java.net.Authenticator.setDefault(new java.net.Authenticator()
		{
			private PasswordAuthentication authentication = new PasswordAuthentication(user, password.toCharArray());

			@Override
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return authentication;
			}
		});


		OkHttpClient client = new OkHttpClient.Builder().proxy(proxyTest).build();
//			Request request = new Request.Builder().url("http://192.168.88.198:1823/socktest").build();
		Request request = new Request.Builder().url("http://www.baidu.com").addHeader("connection","Keep-Alive").build();
		Response response = client.newCall(request).execute();
		System.out.println(response.code());
		System.out.println(response.body().string());
		client.dispatcher().executorService().shutdown();
		client.connectionPool().evictAll();

		

	}

}
