package blog.controller;

import blog.common.vo.UserInfo;
import blog.entity.User;
import blog.service.UserService;
import blog.util.ResultUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.RequestBody;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 分页查询用户
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/list")
    public ResultUtil userList(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize){
        List<UserInfo> list = userService.getUserInfoList();
        int size = list.size();
        if(size == 0)
            return ResultUtil.fail("数据库中没有用户");
        Page page = new Page(currentPage,pageSize);
        if (pageSize > size)
            pageSize = size;
        // 求出最大页数，防止currentPage越界
        int maxPage = size % pageSize == 0 ? size / pageSize : size / pageSize + 1;
        if (currentPage > maxPage)
            currentPage = maxPage;
        // 当前页第一条数据的下标
        int curIdx = currentPage > 1 ? (currentPage - 1) * pageSize : 0;
        List pageList = new ArrayList();
        // 将当前页的数据放进pageList
        for (int i = 0; i < pageSize && curIdx + i < size; i++)
            pageList.add(list.get(curIdx + i));
        page.setTotal(list.size()).setRecords(pageList);
        return ResultUtil.success(page);
    }

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:create")
    @RequiresAuthentication
    @PostMapping("/create")
    public ResultUtil createUser(@Validated @RequestBody User user){
        if(null == user)
            return ResultUtil.fail("不能为空");
        else {
            if(user.getRole().contains("role_root"))
                return ResultUtil.fail("没有设置root用户的权限");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setPassword(SecureUtil.md5(user.getPassword()));
            userService.saveOrUpdate(user);
        }
        return ResultUtil.success(null);
    }

    /**
     * 更改用户信息
     * @param user
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:update")
    @RequiresAuthentication
    @PostMapping("/update")
    public ResultUtil updateUser(@Validated @RequestBody User user){
        System.out.println(user);
        if(null == user)
            return ResultUtil.fail("不能为空");
        else {
            user.setUpdateTime(LocalDateTime.now());
            User subUser = userService.getById(user.getId());
            if(user.getPassword() == null)
                user.setPassword(subUser.getPassword());
            else
                user.setPassword(SecureUtil.md5(user.getPassword()));
            userService.saveOrUpdate(user);
        }
        return ResultUtil.success(null);
    }

    /**
     * 删除用户
     * @param id
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @GetMapping("/delete/{id}")
    public ResultUtil delete(@PathVariable(name = "id") Long id) {
        User user = userService.getById(id);
        if(user.getRole().equals("role_root"))
            return ResultUtil.fail("禁止删除此用户");
        if (userService.removeById(id))
            return ResultUtil.success(null);
        else
            return ResultUtil.fail("删除失败");
    }

    /**
     * 修改用户的状态
     * @param id
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:update")
    @RequestMapping("/publish/{id}")
    public ResultUtil publish(@PathVariable(name = "id")Long id){
        User user = userService.getById(id);
        if(user.getRole().equals("role_root"))
            return ResultUtil.fail("禁止禁用此用户");
        if (user.getStatus()==0)
            user.setStatus(1);
        else
            user.setStatus(0);
        userService.saveOrUpdate(user);
        return ResultUtil.success(null);
    }
}
