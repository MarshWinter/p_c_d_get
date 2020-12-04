package com.example.httputil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * http请求工具类
 * github地址：https://github.com/zhuzhegithub/utils
 * @author zhuzhe
 * @date 2018/5/3 11:46
 */
public class HttpUtils {
	/**
	 * 普通http连接的连接创建类
	 */
	private static HttpClientBuilder httpClientBuilder;
	/**
	 * ssl连接的连接创建类
	 */
	private static HttpClientBuilder sslHttpClientBuilder;

	/**
	 * get
	 *
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doGet(String host, String path, Map<String, String> headers, Map<String, String> querys)
			throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpGet request = new HttpGet(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		return httpClient.execute(request);
	}

	/**
	 * post form
	 *
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @param bodys
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doPost(String host, String path, Map<String, String> headers, Map<String, String> querys,
			Map<String, String> bodys) throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpPost request = new HttpPost(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		if (bodys != null) {
			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();

			for (String key : bodys.keySet()) {
				nameValuePairList.add(new BasicNameValuePair(key, bodys.get(key)));
			}
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairList, "utf-8");
			formEntity.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
			request.setEntity(formEntity);
		}
		return httpClient.execute(request);
	}

	/**
	 * Post String
	 *
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doPost(String host, String path, Map<String, String> headers, Map<String, String> querys,
			String body) throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpPost request = new HttpPost(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		if (StringUtils.isNotBlank(body)) {
			request.setEntity(new StringEntity(body, "utf-8"));
		}
		return httpClient.execute(request);
	}

	/**
	 * Post stream
	 *
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doPost(String host, String path, Map<String, String> headers, Map<String, String> querys,
			byte[] body) throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpPost request = new HttpPost(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		if (body != null) {
			request.setEntity(new ByteArrayEntity(body));
		}
		return httpClient.execute(request);
	}

	/**
	 * Put String
	 * 
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doPut(String host, String path, Map<String, String> headers, Map<String, String> querys,
			String body) throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpPut request = new HttpPut(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		if (StringUtils.isNotBlank(body)) {
			request.setEntity(new StringEntity(body, "utf-8"));
		}
		return httpClient.execute(request);
	}

	/**
	 * Put stream
	 * 
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doPut(String host, String path, Map<String, String> headers, Map<String, String> querys,
			byte[] body) throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpPut request = new HttpPut(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		if (body != null) {
			request.setEntity(new ByteArrayEntity(body));
		}
		return httpClient.execute(request);
	}

	/**
	 * Delete
	 *
	 * @param host
	 * @param path
	 *
	 * @param headers
	 * @param querys
	 * @return
	 * @throws Exception
	 */
	public static HttpResponse doDelete(String host, String path, Map<String, String> headers,
			Map<String, String> querys) throws Exception {
		HttpClient httpClient = wrapClient(host, path);
		HttpDelete request = new HttpDelete(buildUrl(host, path, querys));
		for (Map.Entry<String, String> e : headers.entrySet()) {
			request.addHeader(e.getKey(), e.getValue());
		}
		return httpClient.execute(request);
	}

	/**
	 * 构建请求的 url
	 * 
	 * @param host
	 * @param path
	 * @param querys
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String buildUrl(String host, String path, Map<String, String> querys)
			throws UnsupportedEncodingException {
		StringBuilder sbUrl = new StringBuilder();
		if (!StringUtils.isBlank(host)) {
			sbUrl.append(host);
		}
		if (!StringUtils.isBlank(path)) {
			sbUrl.append(path);
		}
		if (null != querys) {
			StringBuilder sbQuery = new StringBuilder();
			for (Map.Entry<String, String> query : querys.entrySet()) {
				if (0 < sbQuery.length()) {
					sbQuery.append("&");
				}
				if (StringUtils.isBlank(query.getKey()) && !StringUtils.isBlank(query.getValue())) {
					sbQuery.append(query.getValue());
				}
				if (!StringUtils.isBlank(query.getKey())) {
					sbQuery.append(query.getKey());
					if (!StringUtils.isBlank(query.getValue())) {
						sbQuery.append("=");
						sbQuery.append(URLEncoder.encode(query.getValue(), "utf-8"));
					}
				}
			}
			if (0 < sbQuery.length()) {
				sbUrl.append("?").append(sbQuery);
			}
		}
		return sbUrl.toString();
	}

	/**
	 * 获取 HttpClient
	 * 
	 * @param host
	 * @param path
	 * @return
	 */
	private static HttpClient wrapClient(String host, String path) {
		
		if (host != null && host.startsWith("https://")) {
			return sslClient();
		} else if (StringUtils.isBlank(host) && path != null && path.startsWith("https://")) {
			return sslClient();
		} else {//非ssl连接
			if(null == httpClientBuilder) {//单例模式
				synchronized (HttpUtils.class) {
					if(null == httpClientBuilder) {
						//增加httpclient的超时时间
						RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(500)// 连接超时时间
								.setSocketTimeout(50000)// 请求获取数据的超时时间
								.build();
						httpClientBuilder = HttpClientBuilder.create().setMaxConnTotal(HttpConfig.maxConnTotal) // 连接池中最大连接数
								/**
								 * 分配给同一个route(路由)最大的并发连接数。 route：运行环境机器 到 目标机器的一条线路。
								 * 举例来说，我们使用HttpClient的实现来分别请求 www.baidu.com 的资源和 www.bing.com
								 * 的资源那么他就会产生两个route。
								 */
//						        .setMaxConnPerRoute(config.maxConnPerRoute)  
								.setDefaultRequestConfig(requestConfig);
					}
				}
			} 
			return httpClientBuilder.build();
		}
	}

	/**
	 * 在调用SSL之前需要重写验证方法，取消检测SSL 创建ConnectionManager，添加Connection配置信息
	 * 
	 * @return HttpClient 支持https
	 */
	private static HttpClient sslClient() {
		try {
			if(null == sslHttpClientBuilder) {
				synchronized (HttpUtils.class) {
					if(null == sslHttpClientBuilder) {
						// 在调用SSL之前需要重写验证方法，取消检测SSL
						X509TrustManager trustManager = new X509TrustManager() {
							@Override
							public X509Certificate[] getAcceptedIssuers() {
								return null;
							}

							@Override
							public void checkClientTrusted(X509Certificate[] xcs, String str) {
							}

							@Override
							public void checkServerTrusted(X509Certificate[] xcs, String str) {
							}
						};
						SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
						ctx.init(null, new TrustManager[] { trustManager }, null);
						SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(ctx,
								NoopHostnameVerifier.INSTANCE);
						// 创建Registry
						RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT)
								.setConnectTimeout(500)// 连接超时时间
								.setSocketTimeout(5000)// 请求获取数据的超时时间
								.setExpectContinueEnabled(Boolean.TRUE)
								.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
								.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
						Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
								.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();
						// 创建ConnectionManager，添加Connection配置信息
						PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
								socketFactoryRegistry);
						connectionManager.setMaxTotal(HttpConfig.maxConnTotal);
						sslHttpClientBuilder = HttpClientBuilder.create().setConnectionManager(connectionManager)
						.setDefaultRequestConfig(requestConfig).setRetryHandler(new HttpRequestRetryHandler() {
				            @Override
				            public boolean retryRequest(IOException e, int retryTimes, HttpContext httpContext) {
				            	System.out.println("重试");
				                if (retryTimes > 3) {
				                    return false;
				                }
				                if (e instanceof UnknownHostException || e instanceof ConnectTimeoutException
				                        || !(e instanceof SSLException) || e instanceof NoHttpResponseException) {
				                    return true;
				                }

				                HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
				                HttpRequest request = clientContext.getRequest();
				                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				                if (idempotent) {
				                    // 如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
				                    return true;
				                }
				                return false;
				            }
				        });
					}
				}
			}
			CloseableHttpClient closeableHttpClient = sslHttpClientBuilder.build();
			return closeableHttpClient;
		} catch (KeyManagementException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 将结果转换成JSONObject
	 * 
	 * @param httpResponse
	 * @return
	 * @throws IOException
	 */
	public static JSONObject getJson(HttpResponse httpResponse) throws IOException {
		HttpEntity entity = httpResponse.getEntity();
		String resp = EntityUtils.toString(entity, "UTF-8");
		EntityUtils.consume(entity);
		return JSON.parseObject(resp);
	}
}
