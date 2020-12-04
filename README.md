# p_c_d_get


## 基本介绍

利用提供的第三方api从高德地图和阿里云的datav项目获取省市区相关数据，且只涉及省市区，整理成geoJson以及省市区数据库数据

其中datav网址为https://datav.aliyun.com/tools/atlas/#&lat=31.886295751669298&lng=106.720060693723&zoom=4.5
主要用来获取使用echarts服务的地图geoJson数据

高德地图主要用来获取行政区划，同时结合省市区数据库设计生成了一个Mysql的load命令可以使用的省市区规划txt文件


## 文件内容介绍

[生成的文件参考](./generated "生成的文件")包含

- [generated/geoJson](./generated/geoJson)：geoJson数据
- [generated/adNameCodeMap](./generated/adNameCodeMap)：用于echarts的行政区划名与adcode对应的map
- [generated/p_c_d](./generated/p_c_d)：省市区数据库表数据

具体的省市区表设计采用单表设计，具体字段参考如下，


```sql
CREATE TABLE `areas` (
  `adcode` varchar(45) DEFAULT NULL COMMENT '行政区划id,即adcode',
  `parent_adcode` varchar(45) DEFAULT NULL COMMENT '父级行政区域的adcode',
  `rank` tinyint(4) DEFAULT NULL COMMENT '行政级别，1-省，2-市，3-区',
  `level` varchar(45) DEFAULT NULL COMMENT '行政级别的英文名称，province，city，district',
  `name` varchar(45) DEFAULT NULL COMMENT '行政区域名称',
  `parent_path` varchar(45) DEFAULT NULL COMMENT '父级adcoe路径',
  `name_path` varchar(45) DEFAULT NULL COMMENT '父级名称路径',
  `province` varchar(45) DEFAULT NULL COMMENT '所在省',
  `city` varchar(45) DEFAULT NULL COMMENT '所在市',
  `district` varchar(45) DEFAULT NULL COMMENT '所在区',
  `lng` varchar(45) DEFAULT NULL COMMENT '行政区域中心经度',
  `lat` varchar(45) DEFAULT NULL COMMENT '行政区域中心纬度'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```


声明：仅供学习参考！


