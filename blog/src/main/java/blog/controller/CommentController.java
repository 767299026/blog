package blog.controller;

import blog.annotation.AccessLimit;
import blog.annotation.VisitLogger;
import blog.common.vo.PageComment;
import blog.entity.Comment;
import blog.service.CommentService;
import blog.service.MailService;
import blog.util.ResultUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("all")
@RestController
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private MailService mailService;

    Logger logger = LoggerFactory.getLogger(CommentController.class);

    /**
     * 分页查询所有评论
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/commentList")
    public ResultUtil getCommentListByPage(@RequestParam(defaultValue = "1") Integer currentPage, @RequestParam(defaultValue = "10") Integer pageSize) {
        Page page = new Page(currentPage, pageSize);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Comment::getCreateTime);
        IPage pageData = commentService.page(page, queryWrapper);
        return ResultUtil.success(pageData);
    }

    /**
     * 分页查询某个博客下的根评论
     *
     * @param blogId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequiresAuthentication
    @RequiresPermissions("user:read")
    @GetMapping("/comment/detail")
    public ResultUtil getCommentListByPageId(
            @RequestParam(defaultValue = "1") Long blogId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page page = new Page(currentPage, pageSize);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getBlogId, blogId).orderByDesc(Comment::getCreateTime);
        IPage pageData = commentService.page(page, queryWrapper);
        return ResultUtil.success(pageData);
    }

    /**
     * 获取某个博客下的所有评论
     * @param blogId
     * @return
     */
    @GetMapping("/comment/{blogId}")
    public ResultUtil getCommentByBlogId(@PathVariable(name = "blogId") Long blogId) {
        //实体模型集合对象转换为VO对象集合
        List<PageComment> pageCommentList = commentService.getPageCommentListByDesc(blogId,(long) -1);
        for (PageComment pageComment : pageCommentList) {
            List<PageComment> reply = commentService.getPageCommentList(blogId, pageComment.getId());
            pageComment.setReplyComments(reply);
        }
        return ResultUtil.success(pageCommentList);
    }

    /**
     * 修改评论的状态
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresPermissions("user:update")
    @RequestMapping("comment/publish/{id}")
    public ResultUtil publish(@PathVariable(name = "id")String id) {
        Comment comment = commentService.getById(id);
        comment.setIsPublished(!comment.getIsPublished());
        commentService.saveOrUpdate(comment);
        return ResultUtil.success(null);
    }

    /**
     * 修改评论
     * @param comment
     * @return
     */
    @RequiresAuthentication
    @RequiresPermissions("user:update")
    @RequestMapping("comment/update")
    public ResultUtil updateById(@Validated @RequestBody Comment comment) {
        if (comment == null)
            return ResultUtil.fail("不能为空");
        commentService.saveOrUpdate(comment);
        return ResultUtil.success(null);
    }

    /**
     * 删除评论
     * @param id
     * @return
     */
    @RequiresRoles("role_root")
    @RequiresPermissions("user:delete")
    @RequiresAuthentication
    @RequestMapping("comment/delete/{id}")
    public ResultUtil delete(@PathVariable(name = "id")String id){
        if (commentService.removeById(id))
            return ResultUtil.success(null);
        else
            return ResultUtil.fail("删除失败");
    }

    /**
     * 提交评论
     */
    @AccessLimit(seconds = 15, maxCount = 1, msg = "15秒内只能提交一次评论")
    @VisitLogger(behavior = "提交评论")
    @PostMapping("/comment/add")
    public ResultUtil edit(@Validated @RequestBody Comment comment, HttpServletRequest request) {

        if (comment.getContent().contains("<script>") || comment.getEmail().contains("<script>") || comment.getNickname().contains("<script>") || comment.getWebsite().contains("<script>")) {
            return ResultUtil.fail("非法输入");
        }
        System.out.println(comment.toString());
        Comment temp = new Comment();
        temp.setCreateTime(LocalDateTime.now());
        temp.setIp(request.getHeader("x-forwarded-for"));
        BeanUtil.copyProperties(comment, temp, "id", "ip", "createTime");
        commentService.saveOrUpdate(temp);

        //博主的回复向被回复者发送提示邮件
        if(comment.getIsAdminComment()==1&&comment.getParentCommentId()!=-1){
            Comment parentComment = commentService.getOne(new QueryWrapper<Comment>().eq("nickname", comment.getParentCommentNickname()));
            String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
            if (parentComment.getEmail().matches(regex)) {
                mailService.sendSimpleMail(parentComment.getEmail(), "Skymo博客评论回复", "您的的评论："+parentComment.getContent()+"\n博主回复内容："+comment.getContent());
                logger.info("邮件发送成功");
            }
        }
        return ResultUtil.success(null);
    }
}

