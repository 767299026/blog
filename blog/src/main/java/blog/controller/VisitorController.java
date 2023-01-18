package blog.controller;


import blog.common.vo.VisitorNum;
import blog.entity.Visitor;
import blog.service.VisitorService;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客前端控制器
 *
 * @author fanfanli
 * @date  2021/4/8
 */
@SuppressWarnings("all")
@RestController
public class VisitorController {

    @Resource
    private VisitorService visitorService;

    @Resource
    RedisUtil redisUtil;

    /**
     * 获取总uv和pv
     */
    @GetMapping("/visitornum")
    public ResultUtil getPvAndUv() {
        if (redisUtil.hasKey(RedisKey.PV_UV)) {
            return ResultUtil.success(redisUtil.hashget(RedisKey.PV_UV, RedisKey.All));
        }
        int uv = visitorService.list().size();
        int pv = visitorService.getPv();
        VisitorNum visitorNum = new VisitorNum(uv,pv);
        redisUtil.hashset(RedisKey.PV_UV, RedisKey.All, visitorNum);
        return ResultUtil.success(visitorNum);
    }


    /**
     * 查询所有游客
     */
    @RequiresPermissions("user:read")
    @RequiresAuthentication
    @RequestMapping("/visitor")
    public ResultUtil getAllVisiorList(){
        List<Visitor> list = visitorService.lambdaQuery().list();

        return ResultUtil.success(list);
    }


    /**
     * 分页查询所有游客
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/visitorList")
    public ResultUtil getVisitorList(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize) {

        Page page = new Page(currentPage, pageSize);
        IPage pageData = visitorService.page(page, new QueryWrapper<Visitor>().orderByDesc("create_time"));
        return ResultUtil.success(pageData);
    }


    /**
     * 根据访问时间 分页查询所有游客
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/visitor/part")
    public ResultUtil getVisitorListByTime(@RequestParam(defaultValue = "") String time,@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize) {
        String[] endStartTime = time.split(",");
        if(endStartTime.length!=2){
            return ResultUtil.fail("时间设置错误");
        }
        Page page = new Page(currentPage, pageSize);
        IPage pageData = visitorService.page(page, new QueryWrapper<Visitor>().le("last_time",endStartTime[1]).ge("last_time",endStartTime[0]).orderByDesc("create_time"));
        return ResultUtil.success(pageData);
    }


    /**
     * 修改某个游客信息
     */
    @RequiresAuthentication
    @PostMapping("/visitor/update")
    public ResultUtil updateVisitLog(@Validated @RequestBody Visitor visitor){
        if(visitor ==null){
            return ResultUtil.fail("不能为空");
        }
        else{
            if(visitor.getId()==null){
                visitor.setLastTime(LocalDateTime.now());
                visitor.setCreateTime(LocalDateTime.now());
            }
            visitorService.saveOrUpdate(visitor);
        }
        return ResultUtil.success(null);
    }


    /**
     * 删除某个游客
     */
    @RequiresRoles("role_root")
    @RequiresAuthentication
    @RequiresPermissions("user:delete")
    @GetMapping("/visitor/delete/{id}")
    public ResultUtil delete(@PathVariable(name = "id") Long id) {

        if (visitorService.removeById(id))
            return ResultUtil.success(null);
        else
            return ResultUtil.fail("删除失败");
    }


}
