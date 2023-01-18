package blog.interceptor;

import blog.annotation.AccessLimit;
import blog.util.RedisUtil;
import blog.util.ResultUtil;
import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 控制接口访问频率
 *
 * @author fanfanli
 * @date 2021/8/11
 */
@SuppressWarnings("all")
@Component
public class AccessLimitInterceptor implements HandlerInterceptor  {

    @Resource
    RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            //方法上没有访问控制的注解，直接通过
            if (accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            String ip = request.getHeader("x-forwarded-for");;
            String method = request.getMethod();
            String requestURI = request.getRequestURI();
            String redisKey = ip + ":" + method + ":" + requestURI;
            Integer count = (Integer) redisUtil.get(redisKey);

            if (count == null) {
                //在规定周期内第一次访问，存入redis
                redisUtil.set(redisKey, 1);
                redisUtil.expire(redisKey, seconds);
            } else {
                if (count >= maxCount) {
                    //超出访问限制次数
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    ResultUtil result = ResultUtil.fail(403, accessLimit.msg(),null);
                    out.write(String.valueOf(new JSONObject(result)));
                    out.flush();
                    out.close();
                    return false;
                } else {
                    //没超出访问限制次数
                    redisUtil.increment(redisKey, 1);
                }
            }
        }
        return true;
    }
}
