package blog.controller;

import blog.annotation.VisitLogger;
import blog.entity.Friend;
import blog.service.FriendService;
import blog.util.RedisKey;
import blog.util.RedisUtil;
import blog.util.ResultUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@SuppressWarnings("all")
@RestController
public class FriendController {

    @Resource
    private FriendService friendService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 查询所有公开的友链
     */
    @RequestMapping("/friend/all")
    public ResultUtil getFriendList(){
        if (redisUtil.hasHashKey(RedisKey.FRIEND_INFO_CACHE, RedisKey.All))
            return ResultUtil.success(redisUtil.hashget(RedisKey.FRIEND_INFO_CACHE, RedisKey.All));
        List<Friend> list = friendService.lambdaQuery().eq(Friend::getIsPublished, 1).list();
        redisUtil.hashset(RedisKey.FRIEND_INFO_CACHE, RedisKey.All,list);
        return ResultUtil.success(list);
    }


    /**
     * 分页查询所有友链
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/friendList")
    public ResultUtil friendList(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize) {
        Page page = new Page(currentPage, pageSize);
        LambdaQueryWrapper<Friend> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Friend::getCreateTime);
        IPage pageData = friendService.page(page, queryWrapper);
        return ResultUtil.success(pageData);
    }


    /**
     * 友链浏览次数加一
     */
    @VisitLogger(behavior = "点击友链")
    @RequestMapping("/friend/onclick")
    public ResultUtil addView(@RequestParam(name = "")String nickname ){
        if(nickname.equals(""))
            return ResultUtil.fail("访问出错");
        Friend friend = friendService.getOne(new QueryWrapper<Friend>().eq("nickname",nickname));
        friend.setViews(friend.getViews()+1);
        friendService.saveOrUpdate(friend);
        return ResultUtil.success(null);
    }


    /**
     * 修改友链的状态
     */
    @RequiresAuthentication
    @RequiresPermissions("user:update")
    @RequestMapping("friend/publish/{id}")
    public ResultUtil publish(@PathVariable(name = "id")Long id){
        Friend friend = friendService.getById(id);
        friend.setIsPublished(!friend.getIsPublished());
        friendService.saveOrUpdate(friend);
        redisUtil.delete(RedisKey.FRIEND_INFO_CACHE);
        return ResultUtil.success(null);

    }


    /**
     * 修改友链
     */
    @RequiresPermissions("user:update")
    @RequiresAuthentication
    @PostMapping("/friend/update")
    public ResultUtil updateFriend(@Validated @RequestBody Friend friend){
        if(friend==null)
            return ResultUtil.fail("不能为空");
        else{
            if (friend.getId()==null)
                friend.setCreateTime(LocalDateTime.now());
            friendService.saveOrUpdate(friend);
            redisUtil.delete(RedisKey.FRIEND_INFO_CACHE);
        }
        return ResultUtil.success(null);
    }

    /**
     * 增加友链
     */
    @RequiresPermissions("user:create")
    @RequiresAuthentication
    @PostMapping("/friend/create")
    public ResultUtil createFriend(@Validated @RequestBody Friend friend){
        if(friend==null)
            return ResultUtil.fail("不能为空");
        else{
            if (friend.getId()==null)
                friend.setCreateTime(LocalDateTime.now());
            friendService.saveOrUpdate(friend);
            redisUtil.delete(RedisKey.FRIEND_INFO_CACHE);
        }
        return ResultUtil.success(null);
    }


    /**
     * 删除友链
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @GetMapping("/friend/delete/{id}")
    public ResultUtil delete(@PathVariable(name = "id") Long id) {
        if (friendService.removeById(id)) {
            redisUtil.delete(RedisKey.FRIEND_INFO_CACHE);
            return ResultUtil.success(null);
        } else
            return ResultUtil.fail("删除失败");
    }

}
