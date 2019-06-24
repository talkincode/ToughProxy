package org.toughproxy;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public class HttpProxyClientTest {

	@Test
	public  void testHttp() throws Exception {
		final String user = "test";
		final String password = "test";
		
		Proxy proxyTest = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.88.198", 1880));

//		java.net.Authenticator.setDefault(new java.net.Authenticator()
//		{
//			private PasswordAuthentication authentication = new PasswordAuthentication(user, password.toCharArray());
//
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication()
//			{
//				return authentication;
//			}
//		});


		OkHttpClient client = new OkHttpClient.Builder().proxy(proxyTest).build();
		for(int i = 0; i< 30;i++){
//			Request request = new Request.Builder().url("http://192.168.88.198:1823/socktest").build();
			Request request = new Request.Builder().url("http://www.baidu.com").addHeader("connection","Keep-Alive").build();
			Response response = client.newCall(request).execute();
			System.out.println(response.code());
			System.out.println(response.body().string());

			Thread.sleep(3000);
		}
		client.dispatcher().executorService().shutdown();
		client.connectionPool().evictAll();

		

	}

}
