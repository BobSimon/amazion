# 项目结构
```xml
├─amzics-common         //公共代码,与业务无关
├─amzics-mapper         //dao层
├─amzics-model          //model层: 实体类,异常类,注解,枚举,POJO,校验组
├─amzics-service        //service层: service实现类,业务代码
├─amzics-service-api    //service-api层: service接口
├─amzics-web            //web层: 项目入口,过滤器,拦截器,控制器,定时任务,项目配置 等等
└─generator             //mybatis-plus 生成器
```
# 技术栈
+ SpringBoot 技术栈
+ Mybatis Plus 技术栈
+ Lombok
+ Redis
+ MYSQL
+ Log4J2

# 起始
1. git clone 项目到本地
2. 使用idea打开
3. idea安装lombok插件
4. idea的设置里面，maven 跳过测试
5. 配置Idea的 Run/Debug Configurations 中的两个启动项,分别是dev和prod

# 开发规范
1. common 层只允许存放与业务无关的代码
2. Mapper 层从数据库接口的结果集类型必须是 Bean，除非特殊情况，否则不予许使用 Map 类型接收结果集
3. 单表CUD操作，除了特别情况（eg：批量插入...）之外，必须使用 Mybatis Plus Api 进行操作，单表查询不限制，复杂查询不允许使用 Mybatis Plus Api，必须将 SQL 语句写入 XML 映射文件，
4. service 层发生业务错误，必须通过抛出 BusinessException 来先客户端返回错误信息
5. 第三方 API 调用必须放在对应的 service 层
6. 切面只能织入 service 层，如果要对 Controller 切入代码，使用拦截器
7. Controller 接收的参数一定要使用QO，除非参数及其少量简单)，不可逐个参数接收
8. Controller 方法返回结果必须是 RestRusult 类型
9. 请求参数格式校验不允许在 Controller 方法中进行判断，必须使用SpringBoot提供的校验框架进行校验
10. Controller 获取 Request、Session、Response 对象，必须使用 BaseController 的 getRequest()、getSession()、getResponse()方法获取，不允许在方法参数中绑定
11. 登录后才能进行操作的方法上必须标记`@Authen`注解
12. 生产环境的日志不允许清空

# 团队协作
本项目团队协作采用 Git-flow 进行合作项目开发 [参考](https://www.cnblogs.com/myqianlan/p/4195994.html)