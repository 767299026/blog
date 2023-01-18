package blog.controller;

import blog.common.dto.LoginDto;
import blog.entity.User;
import blog.service.UserService;
import blog.util.JWTUtil;
import blog.util.ResultUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("all")
@RestController
public class AccountController {

    @Resource
    private JWTUtil jwtUtil;

    @Resource
    private UserService userService;

    /**
     * 登录请求处理
     *
     * @param loginDto
     * @param response
     * @return
     */
    @CrossOrigin
    @PostMapping("/login")
    public ResultUtil login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDto.getUsername());
        User user = userService.getOne(queryWrapper);
        Assert.notNull(user, "用户名或密码错误");
        if (!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword())))
            return ResultUtil.fail("用户名或密码错误");
        if (user.getStatus() == 0)
            return ResultUtil.fail("账户已被禁用");
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        response.setHeader("Authorization", token);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        return ResultUtil.success(MapUtil.builder()
                .put("id", user.getId())
                .put("username", user.getUsername())
                .put("avatar", user.getAvatar())
                .put("email", user.getEmail())
                .put("role", user.getRole())
                .map()
        );
    }

    /**
     * 登出请求处理
     * @return
     */
    @GetMapping("/logout")
    @RequiresAuthentication
    public ResultUtil logout() {
        SecurityUtils.getSubject().logout();
        return ResultUtil.success("退出成功");
    }
}
