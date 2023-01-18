package blog.schedule;

import blog.entity.Blog;
import blog.service.BlogService;
import blog.util.RedisKey;
import blog.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 定时任务
 * @author: fanfanli
 * @date: 2021/8/11
 */
@SuppressWarnings("all")
@Component
@EnableScheduling
@EnableAsync
public class RedisSyncScheduleTask {

    @Resource
    BlogService blogService;

    @Resource
    RedisUtil redisUtil;

    Logger logger = LoggerFactory.getLogger(RedisSyncScheduleTask.class);

    /**
     * 从Redis同步博客文章浏览量到数据库
     */
    @Async
    @Scheduled(fixedDelay = 24*60*60*1000)  //间隔24小时秒
    public void syncBlogViewsToDatabase() {
        logger.info("执行定时任务");
        String redisKey = RedisKey.BLOG_VIEWS_MAP;
        Map blogViewsMap = redisUtil.hashmapget(redisKey);
        if(blogViewsMap.isEmpty()){
            logger.info("缓存中没有数据");
            return;
        }
        Set<String> keys = blogViewsMap.keySet();
        Set<Integer> sets = new HashSet<>();
        for (String string : keys)
            sets.add(Integer.valueOf(string));
        for (Integer key : sets) {
            Integer views = (Integer) blogViewsMap.get(key);
            Blog blog = blogService.getById(key);
            if (blog == null) {
                logger.info("该信息同步失败");
                continue;
            }
            blog.setViews(blog.getViews()+views);
            blogService.saveOrUpdate(blog);
        }
        deleteAllCache();
        logger.info("完成任务");
    }

    /**
     * 清除所有缓存
     */
    //@Async
    //@Scheduled(fixedDelay = 60*1000)
    public void deleteAllCache() {
        redisUtil.deleteAll();
    }


}
