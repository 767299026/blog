package blog.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@SuppressWarnings("all")
@Component
@EnableScheduling
@EnableAsync
@Slf4j
public class ESHeartbeatScheduleTask {

    @Resource
    private ElasticsearchRestTemplate restTemplate;

    @Async
    @Scheduled(fixedDelay = 1*60*60*1000)  //间隔1小时秒
    public void heartbeat() {
        log.info("尝试获取es链接");
        try {
            restTemplate.logVersions();
        } catch (RuntimeException e) {
            log.info("es链接失败");
        }
    }
}
