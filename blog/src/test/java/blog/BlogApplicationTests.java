package blog;

import blog.entity.Tag;
import blog.util.RedisUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SuppressWarnings("all")
@SpringBootTest
class BlogApplicationTests {

    @Resource(name = "LettuceRedisTemplate")
    private RedisTemplate redisTemplate;

    @Test
    void redisTemplateTest(){
        Tag tag = new Tag();
        tag.setId(null);
        tag.setTagName("1");
        redisTemplate.opsForValue().set("tag",tag);
        System.out.println(redisTemplate.opsForValue().get("tag"));
    }




    @Resource
    private RedisUtil redisUtil;

    @Test
    void contextLoads() {
        Tag tag = new Tag();
        tag.setId(null);
        tag.setTagName("1");
        //redisTemplate.opsForValue().set("tag",tag);
        //原生template模板使用（jdk序列化）
        //System.out.println(redisTemplate.opsForValue().get("tag"));
        redisUtil.set("tag",tag);
        //自定义template模板使用（json序列化）
        //System.out.println(redisUtil.get("tag"));
        Tag result = JSONObject.toJavaObject((JSON) redisUtil.get("tag"),Tag.class);
        //FastJSON对象转为Java对象（传入JSON对象（Object转为JSON）和要变成的对象）
        //System.out.println(result);
        String stringResult = JSONObject.toJSONString(redisUtil.get("tag"));
        //Object对象转为字符串
        //System.out.println(stringResult);
    }

    @Test
    void test(){
        int i = 1;
        redisUtil.set("int",i);
        Integer j = (Integer) redisUtil.get("int");
        System.out.println(j);
    }


}
