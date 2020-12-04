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
 * 行政区划获取，省市区，包括adcode，行政区划id，从高德地图获取。
 * 文件输出格式
 * 
 * 区划id, 	父id, 			级别(1,2,3) , 	级别名称(province,city,district,street), 	全称, 		id路径（110000,110100,110105）,   名称路径,		省份全称，	城市全称，	县区全称，	经度，	纬度
 * adcode, 	parent_adcode, 	rank, 			level, 										name, 		parent_path, 					name_path	province, 	city, 		district, 	lng, 	lat
 * 
 * 
 * 
 * @author lxd
 * @version 创建时间:2020年12月3日 下午2:13:11
 */
public class AdministrativeRegionGet {
	
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
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("generated/p_c_d/p_c_d.txt")));
		recursionResolve(districtsArray, writer, chinaJson, parentList);
		writer.flush();
		writer.close();
	}
	
	/**
	 * 递归处理省市区数据，排除街道street信息，只包含省市区数据
	 * @author lxd
	 * @version 创建时间:2020年12月3日 下午2:48:51
	 * @param districts 需要处理的行政区块列表
	 * @param writer 写入的文件，供mysql使用load语句
	 * @param parent 直接上属的父级行政区域
	 * @param parentList 上属父级行政区域list
	 * @throws IOException 
	 */
	public static void recursionResolve(JSONArray districts, BufferedWriter writer, JSONObject parent, List<JSONObject> parentList) throws IOException {
		
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
				//首先保证该条信息写入文本文件
				String rowItem = rowItem(district, parent, parentList);
				writer.write(rowItem);
				writer.newLine();
				
				//本次无需继续递归
				if(district.getJSONArray("districts").size() == 0 || district.getString("level").equals("district")) {
					continue ;
				}
				
				
				//获取子行政单位
				JSONArray subDistricts = district.getJSONArray("districts");
				//将本次递归的本身加入父级行政单位列表
				List<JSONObject> middleParentList = new LinkedList<>(parentList);
				middleParentList.add(district);
				recursionResolve(subDistricts, writer, district, middleParentList);
			}
			
			
		}
		
	}
	
	/**
	 * 整理成一行数据
	 * @author lxd
	 * @version 创建时间:2020年12月3日 下午4:46:32
	 * @param self 处理的数据
	 * @param parent 直接上属的父级行政区域
	 * @param parentList 上属父级行政区域list
	 * @return 处理好的数据
	 *
	 */
	public static String rowItem(JSONObject self, JSONObject parent, List<JSONObject> parentList) {
		StringBuilder sb = new StringBuilder();
		sb.append(self.getString("adcode")).append("\t")
			.append(parent.getString("adcode")).append("\t");
		switch (self.getString("level")) {
			case "province":
				sb.append(1).append("\t")
				.append(self.getString("level")).append("\t");
				break;
			case "city":
				sb.append(2).append("\t")
				.append(self.getString("level")).append("\t");
				break;
			case "district":
				sb.append(3).append("\t")
				.append(self.getString("level")).append("\t");
				break;
	
			default:
				//不是省市区一级的，抛出异常不进行收录
				return "";
		}
		
		sb.append(self.getString("name")).append("\t");
		//区划id路径
		StringBuilder adcodePath = new StringBuilder();
		if(null != parentList) {
			for(int i = 0; i < parentList.size(); i++) {
				if(parentList.get(i).getString("level").equals("country")) {
					continue;
				}
				adcodePath.append(parentList.get(i).getString("adcode")).append(",");
			}
		}
		adcodePath.append(self.getString("adcode"));
		sb.append(adcodePath).append("\t");
		
		//区划名称路径
		StringBuilder namePath = new StringBuilder();
		if(null != parentList) {
			for(int i = 0; i < parentList.size(); i++) {
				if(parentList.get(i).getString("level").equals("country")) {
					continue;
				}
				namePath.append(parentList.get(i).getString("name")).append(",");
			}
		}
		namePath.append(self.getString("name"));
		sb.append(namePath).append("\t");
		
		//循环省市区全称进行输出
		String provinceName = null;
		String cityName = null;
		String districtName = null;
		
		if(null != parentList) {
			for(int i = 0; i < parentList.size(); i++) {
				switch (parentList.get(i).getString("level")) {
				case "province":
					provinceName = parentList.get(i).getString("name");
					break;
				case "city":
					cityName = parentList.get(i).getString("name");
					break;
				case "district":
					districtName = parentList.get(i).getString("name");
					break;
				default:
					break;
				}
				
			}
		}
		switch (self.getString("level")) {
		case "province":
			provinceName = self.getString("name");
			break;
		case "city":
			cityName = self.getString("name");
			break;
		case "district":
			districtName = self.getString("name");
			break;
		default:
			break;
		}
		if(null != provinceName) {
			sb.append(provinceName).append("\t");
		}else {
			sb.append("\t");
		}
		if(null != cityName) {
			sb.append(cityName).append("\t");
		}else {
			sb.append("\t");
		}
		if(null != districtName) {
			sb.append(districtName).append("\t");
		}else {
			sb.append("\t");
		}
		//经纬度,center	"111.618386,33.783195"
		String[] split = self.getString("center").split(",");
		sb.append(split[0]).append("\t")
			.append(split[1]);
		return sb.toString();
	}
	
}
