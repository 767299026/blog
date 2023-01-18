package blog.aspect;

import blog.annotation.VisitLogger;
import blog.entity.Blog;
import blog.entity.VisitLog;
import blog.entity.Visitor;
import blog.service.VisitLogService;
import blog.service.VisitorService;
import blog.util.*;
import cn.hutool.json.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


/**
 * AOP记录访问日志
 *
 * @author fanfanli
 * @date  2021/5/3
 */
@SuppressWarnings("all")
@Component
@Aspect
public class VisitLogAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private VisitLogService visitLogService;

    @Resource
    private VisitorService visitorService;

    @Resource
    private UserAgentUtils userAgentUtils;

    @Resource
    private RedisUtil redisUtil;

    ThreadLocal<Long> currentTime = new ThreadLocal<>();

    /**
     * 配置切入点
     */
    @Pointcut("@annotation(visitLogger)")
    public void log(VisitLogger visitLogger){}

    /**
     * 配置环绕通知
     *
     * @param joinPoint 连接点
	 * @return 返回方法执行后的结果
	 */
    @Around("log(visitLogger)")
    public Object logAround(ProceedingJoinPoint joinPoint,VisitLogger visitLogger) throws Throwable {

        currentTime.set(System.currentTimeMillis());
        //获取请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        //让目标方法执行 获取返回的结果
        Object result = joinPoint.proceed();
        int times = (int) (System.currentTimeMillis() - currentTime.get());
        currentTime.remove();
        //校验访客标识码
        String identification = checkIdentification(request);
        //异步保存至数据库
        saveVisitLog(joinPoint, visitLogger, request, result, times, identification);

        return result;
    }

    void saveVisitLog(){

    }

    /**
     * 异步设置VisitLogger对象属性并保存到数据库中
     *
     * @param joinPoint
     * @param visitLogger
     * @param result
     * @param times
     * @return
     */
    @Async
    void saveVisitLog(ProceedingJoinPoint joinPoint, VisitLogger visitLogger, HttpServletRequest request, Object result,
                               int times, String identification) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String behavior = visitLogger.behavior();
        String content = visitLogger.content();

        String userAgent = request.getHeader("User-Agent");
        Map<String, String> userAgentMap = userAgentUtils.parseOsAndBrowser(userAgent);
        String os = userAgentMap.get("os");
        String browser = userAgentMap.get("browser");

        //获取参数名和参数值
        Map<String, Object> requestParams = new LinkedHashMap<>();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if( args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse || args[i] instanceof MultipartFile){
                continue;
            }
            requestParams.put(parameterNames[i], args[i]);
        }
        //根据访问内容和返回的结果判断访问的内容并进行备注
        Map<String, String> map = judgeBehavior(behavior, content, requestParams, result);
        VisitLog log = new VisitLog(null,identification, uri, method, new JSONObject(requestParams).toString(), behavior, map.get("content"),map.get("remark"),os,browser,LocalDateTime.now(),times, userAgent);

        visitLogService.saveOrUpdate(log);
    }



    /**
     * 根据访问行为，设置对应的访问内容或备注
     *
     * @param behavior
     * @param content
     * @param requestParams
     * @param result
     * @return 返回内容和备注为主键的map
     */
    private Map<String, String> judgeBehavior(String behavior, String content, Map<String, Object> requestParams, Object result) {
        Map<String, String> map = new HashMap<>();
        String remark = "";
        if (behavior.equals("访问页面") && (content.equals("首页"))) {
            int pageNum = (int) requestParams.get("currentPage");
            remark = "第" + pageNum + "页";
        } else if (behavior.equals("查看博客")) {
            ResultUtil res = (ResultUtil) result;
            if (res.getCode() == 200) {
                Blog blog = (Blog) res.getData();
                String title = blog.getTitle();
                content = title;
                remark = "文章标题：" + title;
            }
        } else if (behavior.equals("搜索博客")) {
            ResultUtil res = (ResultUtil) result;
            if (res.getCode() == 200) {
                String query = (String) requestParams.get("queryString");
                content = query;
                remark = "搜索内容：" + query;
            }
        } else if (behavior.equals("查看分类")) {
            String categoryName = (String) requestParams.get("typeName");
            int pageNum = (int) requestParams.get("currentPage");
            content = categoryName;
            remark = "分类名称：" + categoryName + "，第" + pageNum + "页";
        } else if (behavior.equals("点击友链")) {
            String nickname = (String) requestParams.get("nickname");
            content = nickname;
            remark = "友链名称：" + nickname;
        }
        map.put("remark", remark);
        map.put("content", content);
        return map;
    }




    /**
     * 校验访客标识码
     *
     * @param request
     * @return 访客标识码UUID
     */
    private String checkIdentification(HttpServletRequest request) {
        String identification = request.getHeader("identification");
        if (identification == null) {
            //第一次访问，签发uuid并保存到数据库和Redis
            identification =   UUID.randomUUID().toString();
            saveUUID(identification,request);
        } else {
            //校验Redis中是否存在uuid
            boolean redisHas = redisUtil.selectHasKey(RedisKey.IDENTIFICATION_SET, identification);

            //Redis中不存在uuid
            if (!redisHas) {
                //校验数据库中是否存在uuid
                boolean mysqlHas = visitorService.hasUUID(identification);
                if (mysqlHas) {
                    //数据库存在，保存至Redis
                    redisUtil.setSet("identificationSet", identification);
                    //更新最后访问时间和pv
                    updateVistor(identification);

                } else {
                    //数据库不存在，签发新的uuid
                    identification =   UUID.randomUUID().toString();
                    //异步保存
                    saveUUID(identification,request);
                }
            }
            else{
                //更新最后时间和pv
                updateVistor(identification);
            }
        }
        return identification;
    }




    @Async
    void updateVistor(String identification) {
        //更新最后访问时间和pv
        Visitor visitor = visitorService.getVisitorByUuid(identification);
        visitor.setPv(visitor.getPv()+1);
        visitor.setLastTime(LocalDateTime.now());
        visitorService.saveOrUpdate(visitor);
    }


    /**
     * 异步保存UUID至数据库和Redis
     *
     * @param request
     * @return UUID
     */
    @Async
    void saveUUID(String uuid,HttpServletRequest request) {

        //保存至Redis
        redisUtil.setSet(RedisKey.IDENTIFICATION_SET, uuid);
        //获取响应对象
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        //添加访客标识码UUID至响应头
        response.addHeader("identification", uuid);
        //暴露自定义header供页面资源使用
        response.addHeader("Access-Control-Expose-Headers", "identification");

        //获取访问者基本信息
        String ip = request.getHeader("x-forwarded-for");

        String userAgent = request.getHeader("User-Agent");
        Map<String, String> userAgentMap = userAgentUtils.parseOsAndBrowser(userAgent);
        String os = userAgentMap.get("os");
        String browser = userAgentMap.get("browser");
        Visitor visitor = new Visitor(null,uuid,os,browser,LocalDateTime.now(),LocalDateTime.now(),1, userAgent);
        //保存至数据库
        visitorService.saveOrUpdate(visitor);
    }
}
