package com.example.httputil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import org.apache.http.HttpResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class GeoJsonGet {
	
	public static final String BASE_DIR = "generated/geoJson";
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		//datav 获取json的地址https://geo.datav.aliyun.com/areas/bound/geojson?code=100000_full
		HashMap<String, String> paramMap = new HashMap<>();
		HashMap<String, String> headerMap = new HashMap<>();
//		headerMap.put("Connection", "keepalive");
		paramMap.put("code", 100000+"_full");
		HttpResponse doGet2 = HttpUtils.doGet("https://geo.datav.aliyun.com", "/areas/bound/geojson", headerMap, paramMap);
		JSONObject chinaJson = HttpUtils.getJson(doGet2);
		//Thread.sleep(10);
		writeFile("", "china.json", chinaJson.toString());
		JSONArray provinceArray = chinaJson.getJSONArray("features");
		System.out.println("获取中国地图");
		//获取一级行政单位的数据
		for (int i = 0; i<provinceArray.size()-1; i++) {
			JSONObject province = provinceArray.getJSONObject(i).getJSONObject("properties");
			HashMap<String, String> provinceMap = new HashMap<>();
			provinceMap.put("code", province.getString("adcode")+"_full");
			try {
				getProvince(headerMap, province, provinceMap);
			}catch (SocketTimeoutException e) {
				// TODO: response流获取尝试，手动重试一次
				//重试一次
				getProvince(headerMap, province, provinceMap);
			}
		}
	}

	/**
	 * @author lxd
	 * @version 创建时间:2020年12月3日 上午9:52:23
	 * @param headerMap
	 * @param province
	 * @param provinceMap
	 * @throws Exception
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	private static void getProvince(HashMap<String, String> headerMap, JSONObject province,
			HashMap<String, String> provinceMap) throws Exception, IOException, InterruptedException {
		//根据观察这个api获取的数据会较新
		HttpResponse doGet = HttpUtils.doGet("https://geo.datav.aliyun.com", "/areas_v2/bound/"+province.getString("adcode")+"_full.json", headerMap, provinceMap);
		JSONObject provinceJson = HttpUtils.getJson(doGet);
		//Thread.sleep(10);
		writeFile("province", province.getString("adcode")+".json", provinceJson.toString());
		System.out.println( province.getString("name") );
		//获取二级行政单位
		JSONArray cityArray = provinceJson.getJSONArray("features");
		for (int j = 0; j<cityArray.size(); j++) {
			JSONObject city = cityArray.getJSONObject(j).getJSONObject("properties");
			HashMap<String, String> cityeMap = new HashMap<>();
			cityeMap.put("code", city.getString("adcode")+"_full");
			try {
				getCity(headerMap, city);
			}catch(JSONException e) {
				try {
					//没有三级行政单位
					System.err.println(city.getString("name")+":没有三级行政单位");
					cityeMap = new HashMap<>();
					cityeMap.put("code", city.getString("adcode"));
					JSONObject cityJson = HttpUtils.getJson(HttpUtils.doGet("https://geo.datav.aliyun.com", "/areas_v2/bound/"+city.getString("adcode")+".json", headerMap, new HashMap()));
					//Thread.sleep(10);
					writeFile("city", city.getString("adcode")+".json", cityJson.toString());
					System.out.println( "--" + city.getString("name") );
				}catch(Exception noMap) {
					System.err.println(city.getString("name")+"没有具体的地图");
				}
			}catch (SocketTimeoutException e) {
				// TODO: response流获取尝试，手动重试一次
				//重试一次
				getCity(headerMap, city);
			}
		}
	}

	/**
	 * @author lxd
	 * @version 创建时间:2020年12月2日 下午8:42:39
	 * @param headerMap
	 * @param city
	 * @throws Exception
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	private static void getCity(HashMap<String, String> headerMap, JSONObject city)
			throws Exception, IOException, InterruptedException {
		HttpResponse doGet3 = HttpUtils.doGet("https://geo.datav.aliyun.com", "/areas_v2/bound/"+city.getString("adcode")+"_full.json", headerMap, new HashMap<String, String>());
		JSONObject cityJson = HttpUtils.getJson(doGet3);
		//Thread.sleep(10);
		writeFile("city", city.getString("adcode")+".json", cityJson.toString());
		System.out.println( "--" + city.getString("name") );
		//获取三级行政单位
		JSONArray districtArray = cityJson.getJSONArray("features");
		for (int k = 0; k<districtArray.size(); k++) {
			JSONObject district = districtArray.getJSONObject(k).getJSONObject("properties");
			HashMap<String, String> districtMap = new HashMap<>();
			districtMap.put("code", district.getString("adcode")+"_full");
			try {
				getDistrict(headerMap, district);
			} catch(JSONException e){
				//没有下属行政单位地图
				System.err.println(district.getString("name")+":无法获取地图");
			} catch (SocketTimeoutException e) {
				// TODO: handle exception
				//重试一次
				getDistrict(headerMap, district);
			}
		}
	}

	/**
	 * @author lxd
	 * @version 创建时间:2020年12月2日 下午8:46:51
	 * @param headerMap
	 * @param district
	 * @throws Exception
	 * @throws IOException
	 * @throws InterruptedException
	 * 
	 */
	private static void getDistrict(HashMap<String, String> headerMap, JSONObject district)
			throws Exception, IOException, InterruptedException {
		HttpResponse doGet4 = HttpUtils.doGet("https://geo.datav.aliyun.com", "/areas_v2/bound/"+district.getString("adcode")+".json", headerMap, new HashMap<String, String>());
		JSONObject districtJson = HttpUtils.getJson(doGet4);
		//Thread.sleep(10);
		writeFile("district", district.getString("adcode")+".json", districtJson.toString());
		System.out.println("----" + district.getString("name") );
	}
	
	public static void writeFile(String parentDir, String filename, String content) throws IOException {
		File baseFile = new File(BASE_DIR);
		
		File pDir = new File(baseFile,parentDir);
		if(!pDir.exists()) {
			pDir.mkdirs();
		}
		File desFile = new File(pDir,filename);
		FileWriter fw = new FileWriter(desFile);
		fw.write(content);
		fw.flush();
		fw.close();
	}
	
}
