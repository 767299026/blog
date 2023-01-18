package blog.controller;

import blog.entity.Type;
import blog.service.TypeService;
import blog.util.RedisKey;
import blog.util.RedisUtil;
import blog.util.ResultUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@SuppressWarnings("all")
@RestController
public class TypeController {

    @Resource
    private TypeService typeService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 查询所有分类
     * @return
     */
    @GetMapping("/types")
    public ResultUtil blogs(){
        if(redisUtil.hasHashKey(RedisKey.CATEGORY_NAME_CACHE, RedisKey.All))
            return ResultUtil.success(redisUtil.hashget(RedisKey.CATEGORY_NAME_CACHE, RedisKey.All));
        List<Type> list = typeService.list(new QueryWrapper<Type>());
        redisUtil.hashset(RedisKey.CATEGORY_NAME_CACHE, RedisKey.All,list);
        return ResultUtil.success(list);
    }

    /**
     * 分页查询分类
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/type/list")
    public ResultUtil typeList(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize) {

        Page page = new Page(currentPage, pageSize);
        IPage pageData = typeService.page(page, new QueryWrapper<Type>());
        return ResultUtil.success(pageData);
    }


    /**
     * 增加分类
     * @param type
     * @return
     */
    @RequiresPermissions("user:create")
    @RequiresAuthentication
    @PostMapping("/type/create")
    public ResultUtil createType(@Validated @RequestBody Type type){
        if(type==null)
            return ResultUtil.fail("不能为空");
        else{
            typeService.saveOrUpdate(type);
            redisUtil.delete(RedisKey.CATEGORY_NAME_CACHE);
        }
        return ResultUtil.success(null);
    }


    /**
     * 修改分类
     * @param type
     * @return
     */
    @RequiresPermissions("user:update")
    @RequiresAuthentication
    @PostMapping("/type/update")
    public ResultUtil updateType(@Validated @RequestBody Type type){
        if(type==null)
            return ResultUtil.fail("不能为空");
        else{
            typeService.saveOrUpdate(type);
            redisUtil.delete(RedisKey.CATEGORY_NAME_CACHE);
        }
        return ResultUtil.success(null);
    }


    /**
     * 删除分类
     * @param id
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @GetMapping("/type/delete/{id}")
    public ResultUtil delete(@PathVariable(name = "id") Long id) {

        if (typeService.removeById(id)) {
            redisUtil.delete(RedisKey.CATEGORY_NAME_CACHE);
            return ResultUtil.success(null);
        } else {
            return ResultUtil.fail("删除失败");
        }
    }

}
