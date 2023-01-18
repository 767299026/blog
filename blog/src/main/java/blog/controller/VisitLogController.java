package blog.controller;


import blog.entity.VisitLog;
import blog.service.VisitLogService;
import blog.util.ResultUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * 访客记录前端控制器
 *
 * @author fanfanli
 * @date  2021/4/8
 */
@SuppressWarnings("all")
@RestController
public class VisitLogController {

    @Resource
    private VisitLogService visitLogService;

    /**
     * 查询所有游客浏览日志
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @RequestMapping("/visitLog/all")
    public ResultUtil getFriendList(){
        List<VisitLog> list = visitLogService.lambdaQuery().list();
        return ResultUtil.success(list);
    }

    /**
     * 分页查询所有游客
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/visitLogList")
    public ResultUtil getVisitorList(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize) {
        Page page = new Page(currentPage, pageSize);
        LambdaQueryWrapper<VisitLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(VisitLog::getCreateTime);
        IPage pageData = visitLogService.page(page, queryWrapper);
        return ResultUtil.success(pageData);
    }


    /**
     * 根据uuid 访问时间范围 分页查询所有游客日志
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/visitLog/part")
    public ResultUtil getVisitorList(
            @RequestParam(defaultValue = "") String uuid,
            @RequestParam(defaultValue = "") String time,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        String[] endStartTime = time.split(",");
        if(time.equals("")&&uuid.equals("")){
            Page page = new Page(currentPage, pageSize);
            LambdaQueryWrapper<VisitLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(VisitLog::getCreateTime);
            IPage pageData = visitLogService.page(page, queryWrapper);
            return ResultUtil.success(pageData);
        }
        if(time.equals("")){
            Page page = new Page(currentPage, pageSize);
            LambdaQueryWrapper<VisitLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitLog::getUuid,uuid).orderByDesc(VisitLog::getCreateTime);
            IPage pageData = visitLogService.page(page, queryWrapper);
            return ResultUtil.success(pageData);
        }
        else if(uuid.equals("")){
            Page page = new Page(currentPage, pageSize);
            LambdaQueryWrapper<VisitLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.le(VisitLog::getCreateTime,endStartTime[1])
                    .ge(VisitLog::getCreateTime,endStartTime[0])
                    .orderByDesc(VisitLog::getCreateTime);
            IPage pageData = visitLogService.page(page, queryWrapper);
            return ResultUtil.success(pageData);
        }
        else{
            Page page = new Page(currentPage, pageSize);
            LambdaQueryWrapper<VisitLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitLog::getUuid,uuid)
                    .le(VisitLog::getCreateTime,endStartTime[1])
                    .ge(VisitLog::getCreateTime,endStartTime[0])
                    .orderByDesc(VisitLog::getCreateTime);
            IPage pageData = visitLogService.page(page, queryWrapper);
            return ResultUtil.success(pageData);
        }

    }


    /**
     * 修改游客访问日志
     */
    @RequiresAuthentication
    @PostMapping("/visitLog/update")
    public ResultUtil updateVisitLog(@Validated @RequestBody VisitLog visitLog){
        if(visitLog ==null)
            return ResultUtil.fail("不能为空");
        else{
            if(visitLog.getId()==null)
                visitLog.setCreateTime(LocalDateTime.now());
            visitLogService.saveOrUpdate(visitLog);
        }
        return ResultUtil.success(null);
    }


    /**
     * 删除某个浏览日志
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @GetMapping("/visitLog/delete/{id}")
    public ResultUtil delete(@PathVariable(name = "id") Long id) {

        if (visitLogService.removeById(id))
            return ResultUtil.success(null);
        else
            return ResultUtil.fail("删除失败");
    }
}
