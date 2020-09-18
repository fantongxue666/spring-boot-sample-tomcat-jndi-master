package sample.tomcat.jndi.TestMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;
import java.util.Map;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName TestMapper.java
 * @Description TODO
 * @createTime 2020年09月17日 21:21:00
 */
@Mapper
public interface TestMapper {

    @Select(value = "select * from content")
    List<Map> getList();
}
