## 一，什么是JNDI数据源？



> 我们看下百度百科的描述
>
> JNDI（Java Naming and Directory Interface,Java命名和目录接口）是SUN公司提供的一种标准的Java命名系统接口，JNDI提供统一的客户端API，通过不同的访问提供者接口JNDI服务供应接口(SPI)的实现，由管理者将JNDI API映射为特定的命名服务和目录系统，使得Java应用程序可以和这些命名服务和目录服务之间进行交互。
>
> 看完了之后，是不是感觉很抽象？这里我们需要理解这么几点
>
> （1）JNDI是J2EE的规范之一。
>
> （2）JNDI主要有两部分组成：应用程序编程接口和服务供应商接口。应用程序编程接口提供了Java应用程序访问各种命名和目录服务的功能，服务供应商接口提供了任意一种服务的供应商使用的功能。
>
> （3）J2EE 规范要求全部 J2EE 容器都要提供 JNDI 规范的实现。
>
> （4）JNDI 提出的目的是为了解藕，避免了程序与数据库之间的紧耦合，使应用更加易于配置、易于部署。



<font color="red">以上概念内容摘抄自大佬博客，以下内容皆为笔者亲历亲为所得所感悟！</font>

*JNDI的出现，让数据库连接代码交给容器管理，比如Tomcat、JBOSS等容器，这样对于开发者就不用关心数据库配置是什么，使用的什么数据库驱动连接数据库等*

传统的JNDI是依赖web容器的，例如spring框架，在容器中配置好数据源（或者多数据源）配置，然后在spring项目的配置文件中指定数据源名称，然后把spring项目打成war包部署到容器中运行，就可以使用JNDI数据源了！

spring项目外置tomcat集成jndi数据源请参考

https://www.cnblogs.com/springboot-wuqian/archive/2004/01/13/9418180.html

https://www.java4s.com/spring-boot-tutorials/spring-boot-configure-datasource-using-jndi-with-example/

https://cnsyear.com/posts/9b26f7c2.html

https://cnsyear.com/posts/ceb1797.html

<font color="#F4A460">但今天的重点是springboot集成jndi数据源，其实按道理来说，springboot使用的是内置tomcat，也应该可以集成jndi，结果是可以，不过真的走了很多弯路，跳了很多坑，今天就来给大家讲一下springboot内置tomcat如何集成jndi数据源！</font>

## 二，springboot内置tomcat开始集成JNDI

### 1，添加依赖

环境所需依赖包括：mysql驱动，mybatis-starter

```xml
		<dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.2</version>
		</dependency>
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
		</dependency>
```

上面的依赖是必需的，连接和操作数据库少不了的，关键是下面的tomcat依赖，竟然卡了我一天，没有它还真不行，如果没有它的话，当你配置好了数据源信息并注入到spring容器中，会报错提示找不到数据源，亦或者是无法创建数据源

```xml
		<dependency>
			<groupId>org.apache.tomcat</groupId>
			<artifactId>tomcat-dbcp</artifactId>
			<version>${tomcat.version}</version>
		</dependency>
```

### 2，启动类

需要在启动类上加一个注解

```
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
```

这个注解的作用是：在springboot启动并自动配置时排除自动配置数据源信息，排除之后，在application.yml或application.properties中即使没有配置dataSource仍正常运行，否则会因为自动配置了数据源而找不到数据源的配置报错！

<font color="blue">上面的是同行们的经验！但是我发现，即使不配置好像也没关系！一脸懵逼</font>

### 3，配置dataSource数据源信息

创建一个数据源配置类，配置数据源信息，多个数据源可以配置多个Resource

```java
@Configuration
public class DBConfiguration {
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                tomcat.enableNaming();//启用默认禁用的JNDI命名
                return super.getTomcatWebServer(tomcat);
            }
            @Override
            protected void postProcessContext(Context context) {
                //数据源 1
                //构建一个ContextResource对象，然后添加到Context对象中
                ContextResource resource = new ContextResource();
                resource.setName("jdbc/MyFirstMySql");
                resource.setType(DataSource.class.getName());
                resource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                resource.setProperty("url", "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");
                resource.setProperty("username", "root");
                resource.setProperty("password","1234");
                context.getNamingResources().addResource(resource);

                //数据源 2
                //构建一个ContextResource对象，然后添加到Context对象中
                ContextResource resource1 = new ContextResource();
                resource1.setName("jdbc/MySecondMySql");
                resource1.setType(DataSource.class.getName());
                resource1.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                resource1.setProperty("url", "jdbc:mysql://localhost:3306/guli?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");
                resource1.setProperty("username", "root");
                resource1.setProperty("password","1234");
                context.getNamingResources().addResource(resource1);

                super.postProcessContext(context);
            }
        };
        return tomcat;
    }
}
```

### 4，配置yml文件

只需要在yml文件中指定要使用的数据源名称即可，其他的不需要配置

```yml
spring:
  datasource:
    jndi-name: jdbc/MySecondMySql
```

到这里其实集成jndi数据源就已经结束了，就可以使用了，下面来进行测试！

### 5，开始测试

创建mapper文件，并注入到ctrl中进行测试，运行项目访问

<font color="red">content这张表在上面的两个数据库中同时存在，不过在guli数据库中是多条数据，而在test数据库中是只有一条数据，这样可以区分开来看效果！</font>

```java
@Mapper
public interface TestMapper {
    @Select(value = "select * from content")
    List<Map> getList();
}

```

```java
	@Autowired
	TestMapper testMapper;
	@RequestMapping("/getList")
	@ResponseBody
	public List<Map> getList(){
		return testMapper.getList();
	}
```

首先更改yml配置的jndi数据源名称为MyFirstMySql，重启项目，访问接口http://localhost:8080/getList

![](https://img-blog.csdnimg.cn/20200918094940650.png)

更改yml配置的jndi数据源名称为MySecondMySql，重启项目，访问接口http://localhost:8080/getList

![](https://img-blog.csdnimg.cn/20200918095104712.png)



## 三，总结

以下几种情况分别集成jndi数据源区别还是很大的，多注意一下！

> 1，spring外部容器集成jndi
>
> 2，springboot外部容器集成jndi
>
> 3，springboot1.X内部容器集成jndi
>
> 4，springboot2.X内部容器集成jndi

详情参考

https://www.cnblogs.com/springboot-wuqian/archive/2004/01/13/9540439.html

就到这里了，再见！