package com.example.httputil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * 获取map数据
 * @author lxd
 * @version 创建时间:2020年12月4日 下午3:29:26
 */
public class AdNameCodeMapGet {
	
	/**
	 * 省信息
	 */
	public static BufferedWriter provinceWriter = null;
	/**
	 * 市
	 */
	public static BufferedWriter cityWriter = null;
	/**
	 * 区
	 */
	public static BufferedWriter districtWriter = null;
	
	
	public static void main(String[] args) throws Exception {
		HashMap<String, String> headers = new HashMap<String,String>();
		HashMap<String, String> querys = new HashMap<String,String>();
		querys.put("keywords", "中国");
		querys.put("subdistrict", "5");
		querys.put("key", "自己申请的高德应用key");
		//获取中国的行政区划，总共获取到三级
		//部分省直辖市和城市没有区县一级的概念，
		JSONObject chinaJson = HttpUtils.getJson(HttpUtils.doGet("https://restapi.amap.com", "/v3/config/district", headers, querys));
		JSONArray districtsArray = chinaJson.getJSONArray("districts");
		
		List<JSONObject> parentList = new LinkedList<JSONObject>();
		provinceWriter = new BufferedWriter(new FileWriter(new File("generated/adNameCodeMap/provinceMap.txt")));
		cityWriter = new BufferedWriter(new FileWriter(new File("generated/adNameCodeMap/cityMap.txt")));
		districtWriter = new BufferedWriter(new FileWriter(new File("generated/adNameCodeMap/districtMap.txt")));
		//编写前缀
		provinceWriter.write("var provinceMap = {");
		provinceWriter.newLine();;
		cityWriter.write("var cityMap = {");
		cityWriter.newLine();;
		districtWriter.write("var districtMap = {");
		districtWriter.newLine();
		
		//从全国开始获取
		recursionResolve(districtsArray, chinaJson, parentList, 0);
		
		//后缀处理
		provinceWriter.write("}");
		cityWriter.write("}");
		districtWriter.write("}");
		
		
		//关闭流，刷新到磁盘
		provinceWriter.close();
		cityWriter.close();
		districtWriter.close();
	}
	
	/**
	 * 递归处理省市区数据，排除街道street信息，只包含省市区数据
	 * @author lxd
	 * @version 创建时间:2020年12月3日 下午2:48:51
	 * @param districts 需要处理的行政区块列表
	 * @param parent 直接上属的父级行政区域
	 * @param parentList 上属父级行政区域list
	 * @param level 级别0-全国，1-省，2-市，3-区，决定对应的writer
	 * @throws IOException 
	 */
	public static void recursionResolve(JSONArray districts, JSONObject parent, List<JSONObject> parentList, int level) throws IOException {
		
		if(districts !=null && districts.size()>0) {
			for(int i = 0; i < districts.size(); i++) {
				
				//获取其中一个行政区划单位
				JSONObject district = districts.getJSONObject(i);
				//调试用代码
//				if(district.getString("name").equals("阿拉尔市")) {
//					System.out.println("1");
//				}
				
				//如果是街道信息则不进行处理
				if(district.getString("level").equals("street")) {
					continue;
				}
				
				//首先保证该条信息写入文本文件,只处理省市区数据
				String line = "";
				switch (level) {
				case 1://省
					line = rowItem(district, level);
					provinceWriter.write(line);
					provinceWriter.newLine();
					break;
				case 2://市
					line = rowItem(district, level);
					cityWriter.write(line);
					cityWriter.newLine();
					break;
				case 3://区
					line = rowItem(district, level);
					districtWriter.write(line);
					districtWriter.newLine();
					break;

				default:
					break;
				}
				
				
				//本次无需继续递归条件
				if(district.getJSONArray("districts").size() == 0 || district.getString("level").equals("district")) {
					continue ;
				}
				
				
				//获取子行政单位
				JSONArray subDistricts = district.getJSONArray("districts");
				//将本次递归的本身加入父级行政单位列表
				List<JSONObject> middleParentList = new LinkedList<>(parentList);
				middleParentList.add(district);
				recursionResolve(subDistricts, district, middleParentList, level+1);
			}
			
			
		}
		
	}
	
	/**
	 * 整理成一行数据
	 * @author lxd
	 * @version 创建时间:2020年12月4日 下午4:03:14
	 * @param self 需要写入字符串的对象
	 * @param level 行政等级
	 * @return
	 *
	 */
	public static String rowItem(JSONObject self, int level) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t")
		.append("\"").append(self.getString("name")).append("\"")
		.append(":")
		.append("\"").append(self.getString("adcode")).append("\"")
		.append(",");
		return sb.toString();
	}
	
	
	/**
	 * 根据省市区级别分别获取相关writer
	 * @author lxd
	 * @version 创建时间:2020年12月4日 下午3:35:32
	 * @param level
	 * @return
	 *
	 */
	public static BufferedWriter getWriter(int level) {
		switch (level) {
		case 1:
			return provinceWriter;
		case 2:
			return cityWriter;
		case 3:
			return districtWriter;
		default:
			return null;
		}
	}
}
