package com.amzics.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.config.rules.PropertyInfo;

import java.util.HashMap;
import java.util.Map;

public class Generator {

    public static void main(String[] args) {
        //项目根路径
        String pathFormat = System.getProperty("user.dir")+"/%s";
        //路径
        Map<String,String> map = new HashMap<>(6);
        map.put("entity_path",String.format(pathFormat,"amzics-model/src/main/java/com/amzics/model/domain"));
        map.put("mapper_path",String.format(pathFormat,"amzics-mapper/src/main/java/com/amzics/mapper"));
        map.put("xml_path",String.format(pathFormat,"amzics-mapper/src/main/resources/mapper"));
        map.put("service_path",String.format(pathFormat,"amzics-service-api/src/main/java/com/amzics/service"));
        map.put("service_impl_path",String.format(pathFormat,"amzics-service/src/main/java/com/amzics/service/impl"));
        map.put("controller_path",String.format(pathFormat,"amzics-web/src/main/java/com/amzics/web/controller"));

        AutoGenerator mpg = new AutoGenerator();
        mpg.setGlobalConfig( //全局配置
                new GlobalConfig()
                        .setFileOverride(false)     //覆盖文件
                        .setActiveRecord(false)     //ddd模式
                        .setEnableCache(false)      //mybatis启用缓存
                        .setBaseResultMap(true)     //生成BaseResultMap
                        .setBaseColumnList(true)    //生成BaseColumnList
                        //后缀
                        .setMapperName("%sMapper")
                        .setXmlName("%sMapper")
                        .setServiceName("%sService")
                        .setServiceImplName("%sServiceImpl")
                        .setControllerName("%sController")
        ).setDataSource(
                new DataSourceConfig()
                        .setDbType(DbType.MYSQL)
                        .setDriverName("com.mysql.jdbc.Driver")
                        .setUsername("steven")
                        .setPassword("6KpynJ00n2tZO&Aqt")
                        .setUrl("jdbc:mysql://120.77.208.152:3306/seeics?characterEncoding=utf8")
                        //类型转换
                        .setTypeConvert(new MySqlTypeConvert() {
                            @Override
                            public PropertyInfo processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                                //boolean
                                if (fieldType.toLowerCase().contains("tinyint") && fieldType.contains("1")) {
                                    return DbColumnType.BOOLEAN;
                                }
                                //datetime
                                if (fieldType.toLowerCase().contains("date")
                                        || fieldType.toLowerCase().contains("datetime")) {
                                    return DbColumnType.DATE;
                                }
                                return super.processTypeConvert(globalConfig, fieldType);
                            }
                        })
        ).setPackageInfo(
                new PackageConfig().setParent("com.amzics").setController("web.controller").setEntity("model.domain").setPathInfo(map)
        ).setStrategy(
                new StrategyConfig()
                        .setTablePrefix(new String[]{"t_"})             //表前缀
                        .setNaming(NamingStrategy.underline_to_camel)   //命名使用下划线分割
                        .setEntityLombokModel(true)                     //启动Lombok
//                        .setEntityBooleanColumnRemoveIsPrefix(true)   //是否把is前缀去掉
                        .setRestControllerStyle(true)                   //RestController
                        .setInclude(new String[]{"t_sys_user"})             // 需要生成的表
                        .setSuperControllerClass("BaseController")
        );
        mpg.execute();
        System.out.println("------------------------------完成------------------------------");
    }
}
