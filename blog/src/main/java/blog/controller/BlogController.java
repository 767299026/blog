package blog.controller;

import blog.annotation.VisitLogger;
import blog.common.vo.BlogInfo;
import blog.entity.Blog;
import blog.service.BlogService;
import blog.service.ElasticSearchService;
import blog.util.RedisKey;
import blog.util.RedisUtil;
import blog.util.ResultUtil;
import blog.util.ShiroUtil;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
@RestController
public class BlogController {

    @Resource
    private BlogService blogService;

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 按置顶、创建时间排序 分页查询公开的博客
     * @param currentPage
     * @return
     */
    @VisitLogger(behavior = "访问页面", content = "首页")
    @GetMapping("/blogs")
    public ResultUtil getBlogsByPage(@RequestParam(defaultValue = "1")Integer currentPage){
        //有缓存直接返回
        if(redisUtil.hasHashKey(RedisKey.BLOG_INFO_CACHE, JSONObject.toJSONString(currentPage)))
            return ResultUtil.success(redisUtil.hashget(RedisKey.BLOG_INFO_CACHE,JSONObject.toJSONString(currentPage)));
        Page page = new Page(currentPage,5);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getStatus,1).orderByDesc(Blog::getCreateTime);
        IPage pageData = blogService.page(page,queryWrapper);
        redisUtil.hashset(RedisKey.BLOG_INFO_CACHE,JSONObject.toJSONString(currentPage),page);
        return ResultUtil.success(pageData);
    }

    /**
     * 按创建时间排序 分类 分页查询公开的博客简要信息列表
     * @param currentPage
     * @param typeName
     * @return
     */
    @VisitLogger(behavior = "查看分类")
    @GetMapping("/blogsByType")
    public ResultUtil blogsByType(@RequestParam(defaultValue = "1")Integer currentPage,@RequestParam String typeName){
        if(redisUtil.hasHashKey(RedisKey.CATEGORY_BLOG_CACHE, typeName+JSONObject.toJSONString(currentPage)))
            return ResultUtil.success(redisUtil.hashget(RedisKey.CATEGORY_BLOG_CACHE, typeName+JSONObject.toJSONString(currentPage)));
        List<BlogInfo> list = blogService.getBlogInfoListByCategoryName(typeName);
        if(list.size() == 0)
            return ResultUtil.fail("该分类下没有博客");
        int pageSize = 10;
        int size = list.size();
        Page page = new Page();
        if (pageSize > size)
            pageSize = size;
        // 求出最大页数，防止currentPage越界
        int maxPage = size % pageSize == 0 ? size / pageSize : size / pageSize + 1;
        if (currentPage > maxPage)
            currentPage = maxPage;
        // 当前页第一条数据的下标
        int curIdx = currentPage > 1 ? (currentPage - 1) * pageSize : 0;
        //将当前页打的数据放进pageList
        List pageList = new ArrayList();
        for (int i = 0; i < pageSize && curIdx + i < size; i++)
            pageList.add(list.get(curIdx + i));
        page.setCurrent(currentPage).setSize(pageSize).setTotal(list.size()).setRecords(pageList);

        redisUtil.hashset(RedisKey.CATEGORY_BLOG_CACHE, typeName+JSONObject.toJSONString(currentPage),page);
        return ResultUtil.success(page);
    }

    /**
     * 按创建时间排序 分页查询所有博客
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequiresPermissions("user:read")
    @GetMapping("/blogList")
    public ResultUtil blogList(@RequestParam(defaultValue = "1")Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize){
        Page page = new Page(currentPage,pageSize);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Blog::getCreateTime);
        IPage pageData = blogService.page(page,queryWrapper);
        return ResultUtil.success(pageData);
    }

    /**
     * 查询所有博客
     * @return
     */
    @RequiresPermissions("user:read")
    @GetMapping("/blog/all")
    public ResultUtil blogAll(){
        List<Blog> list = blogService.lambdaQuery().list();
        return ResultUtil.success(list);
    }

    /**
     * 查询友链的博客
     * @return
     */
    @VisitLogger(behavior = "访问页面",content = "友链")
    @GetMapping("/friends")
    public ResultUtil friends(){
        if(redisUtil.hasHashKey(RedisKey.BLOG_INFO_CACHE,RedisKey.FRIEND_BLOG_CACHE))
            return ResultUtil.success(redisUtil.hashget(RedisKey.BLOG_INFO_CACHE,RedisKey.FRIEND_BLOG_CACHE));
        List<Blog> list = blogService.lambdaQuery().eq(Blog::getTitle,"友情链接").list();
        if(list.isEmpty())
            return ResultUtil.success(null);
        redisUtil.hashset(RedisKey.BLOG_INFO_CACHE,RedisKey.FRIEND_BLOG_CACHE,list.get(0));
        return ResultUtil.success(list.get(0));
    }

    /**
     * 查询关于我的博客
     * @return
     */
    @VisitLogger(behavior = "访问页面",content = "关于我")
    @GetMapping("/about")
    public ResultUtil Mine(){
        if(redisUtil.hasHashKey(RedisKey.BLOG_INFO_CACHE,RedisKey.ABOUT_INFO_CACHE))
            return ResultUtil.success(redisUtil.hashget(RedisKey.BLOG_INFO_CACHE,RedisKey.ABOUT_INFO_CACHE));
        List<Blog> list = blogService.lambdaQuery().eq(Blog::getTitle,"关于博主").list();
        redisUtil.hashset(RedisKey.BLOG_INFO_CACHE,RedisKey.ABOUT_INFO_CACHE,list.get(0));
        return ResultUtil.success(list.get(0));
    }

    /**
     * 按创建时间排序 分页查询所有博客
     * @param currentPage
     * @return
     */
    @VisitLogger(behavior = "访问页面",content = "归档")
    @GetMapping("/blog/archives")
    public ResultUtil getBlogsArchives(@RequestParam(defaultValue = "1")Integer currentPage){
        if(redisUtil.hasHashKey(RedisKey.ARCHIVE_INFO_CACHE,JSONObject.toJSONString(currentPage)))
            return ResultUtil.success(redisUtil.hashget(RedisKey.ARCHIVE_INFO_CACHE,JSONObject.toJSONString(currentPage)));
        Integer pageSize = 15;
        Page page = new Page(currentPage,pageSize);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blog::getStatus,1).orderByDesc(Blog::getCreateTime);
        IPage pageData = blogService.page(page,queryWrapper);
        redisUtil.hashset(RedisKey.ARCHIVE_INFO_CACHE,JSONObject.toJSONString(currentPage),pageData);
        return ResultUtil.success(pageData);
    }

    /**
     * 查询某个公开博客详情
     * @param id
     * @return
     */
    @VisitLogger(behavior = "查看博客")
    @GetMapping("/blog/{id}")
    public ResultUtil publicBlogDetail(@PathVariable(name = "id") Integer id){
        Blog blog = blogService.getById(id);
        Assert.notNull(blog,"该博客已删除!");
        if(blog.getStatus() != 1)
            return ResultUtil.fail("你没有权限查阅此博客");
        if(redisUtil.hashmapget(RedisKey.BLOG_VIEWS_MAP).containsKey(id))
            redisUtil.hashincrement(RedisKey.BLOG_VIEWS_MAP,id,1);
        else
            redisUtil.hashset(RedisKey.BLOG_VIEWS_MAP,JSONObject.toJSONString(id),1);
        return ResultUtil.success(blog);
    }

    /**
     * 查询某个博客详情
     * @param id
     * @return
     */
    @RequiresPermissions("user:read")
    @GetMapping("/blog/detail/{id}")
    public ResultUtil getBlogDetail(@PathVariable(name = "id") Long id) {
        Blog blog = blogService.getById(id);
        Assert.notNull(blog, "该博客已删除！");
        return ResultUtil.success(blog);
    }

    /**
     * 删除某个博客
     * @param id
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @GetMapping("/blog/delete/{id}")
    public ResultUtil deleteBlog(@PathVariable(name = "id") Long id){
        if(blogService.removeById(id)){
            redisUtil.delete(RedisKey.BLOG_INFO_CACHE);
            redisUtil.delete(RedisKey.ARCHIVE_INFO_CACHE);
            redisUtil.delete(RedisKey.CATEGORY_BLOG_CACHE);
            return ResultUtil.success(null);
        }
        else
            return ResultUtil.fail("删除失败");
    }

    /**
     * 修改博客信息
     * @param blog
     * @return
     */
    @RequiresPermissions("user:update")
    @RequiresAuthentication
    @PostMapping("/blog/update")
    public ResultUtil updateBlog(@Validated @RequestBody Blog blog){
        Blog temp = null;
        if(blog.getId() != null)
            temp = blogService.getById(blog.getId());
        else {
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreateTime(LocalDateTime.now());
            temp.setStatus(0);
        }
        temp.setUpdateTime(LocalDateTime.now());
        BeanUtil.copyProperties(blog, temp, "id","userId","createTime","updateTime");
        blogService.saveOrUpdate(temp);
        redisUtil.delete(RedisKey.BLOG_INFO_CACHE);
        redisUtil.delete(RedisKey.ARCHIVE_INFO_CACHE);
        redisUtil.delete(RedisKey.CATEGORY_BLOG_CACHE);
        return ResultUtil.success(null);
    }

    /**
     * 创建博客
     * @param blog
     * @return
     */
    @RequiresPermissions("user:create")
    @RequiresAuthentication
    @PostMapping("/blog/create")
    public ResultUtil createBlog(@Validated @RequestBody Blog blog){
        Blog temp = null;
        if(blog.getId() != null)
            temp = blogService.getById(blog.getId());
        else {
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreateTime(LocalDateTime.now());
        }
        temp.setUpdateTime(LocalDateTime.now());
        BeanUtil.copyProperties(blog, temp, "id", "userId", "createTime", "updateTime");
        blogService.saveOrUpdate(temp);
        redisUtil.delete(RedisKey.BLOG_INFO_CACHE);
        redisUtil.delete(RedisKey.ARCHIVE_INFO_CACHE);
        redisUtil.delete(RedisKey.CATEGORY_BLOG_CACHE);
        return ResultUtil.success(null);
    }

    /**
     * 修改博客状态
     * @param id
     * @return
     */
    @RequiresPermissions("user:update")
    @RequestMapping("blog/publish/{id}")
    public ResultUtil publishBlog(@PathVariable(name = "id")String id){
        Blog blog = blogService.getById(id);
        if(blog.getStatus() == 0)
            blog.setStatus(1);
        else
            blog.setStatus(0);
        blogService.saveOrUpdate(blog);
        redisUtil.delete(RedisKey.BLOG_INFO_CACHE);
        redisUtil.delete(RedisKey.ARCHIVE_INFO_CACHE);
        redisUtil.delete(RedisKey.CATEGORY_BLOG_CACHE);
        return ResultUtil.success(null);
    }

    @VisitLogger(behavior = "搜索博客")
    @GetMapping("/search")
    public ResultUtil searchBlog(@RequestParam String queryString){
        return ResultUtil.success(elasticSearchService.searchBlogByTitle(queryString));
    }
}
