package sample.tomcat.jndi.DataBaseConfig;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName FirstMysqlConfig.java
 * @Description TODO
 * @createTime 2020年09月18日 08:59:00
 */
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
                ContextResource resource = new ContextResource();//构建一个ContextResource对象，然后添加到Context对象中
                resource.setName("jdbc/MyFirstMySql");
                resource.setType(DataSource.class.getName());
                resource.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
                resource.setProperty("url", "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");
                resource.setProperty("username", "root");
                resource.setProperty("password","1234");
                context.getNamingResources().addResource(resource);

                //数据源 2
                ContextResource resource1 = new ContextResource();//构建一个ContextResource对象，然后添加到Context对象中
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
