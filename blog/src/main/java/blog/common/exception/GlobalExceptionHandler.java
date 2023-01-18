package blog.common.exception;

import blog.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 全局异常处理
 *
 * @author fanfanli
 * @date 2021/5/3
 */
@SuppressWarnings("all")
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕捉shiro的授权异常
     */
    @ExceptionHandler(value = UnauthorizedException.class)
    @ResponseBody
    public ResultUtil jsonExceptionHandler(HttpServletRequest req, Exception e) {
        log.error("权限不足:-------------->{}", e.getMessage());
        return ResultUtil.fail(403, "权限不足",null);
    }

    /**
     * 捕捉shiro的认证异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(ShiroException.class)
    public ResultUtil handle401(ShiroException e) {
        log.error("未登录访问:-------------->{}", e.getMessage());
        return ResultUtil.fail(401, e.getMessage(), null);
    }

    /**
     * 处理Assert的异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResultUtil handler(IllegalArgumentException e) throws IOException {
        log.error("Assert异常:-------------->{}", e.getMessage());
        return ResultUtil.fail(e.getMessage());
    }

    /**
     * @Validated 方法参数异常处理
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResultUtil handler(MethodArgumentNotValidException e) throws IOException {
        log.error("方法参数异常:-------------->", e);
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        return ResultUtil.fail(objectError.getDefaultMessage());
    }

    /**
     * @Validated 校验错误异常处理
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public ResultUtil handler(RuntimeException e) throws IOException {
        log.error("运行时异常:-------------->", e);
        return ResultUtil.fail(e.getMessage());
    }
}
