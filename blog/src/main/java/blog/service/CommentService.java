package blog.service;

import blog.common.vo.PageComment;
import blog.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

@SuppressWarnings("all")
public interface CommentService extends IService<Comment> {

    /**
     * 通过博客id和父评论id查找所有子评论 并按照时间倒序排序
     * @param blogId
     * @param parentCommentId
     * @return
     */
    List<PageComment> getPageCommentListByDesc(Long blogId, Long parentCommentId);

    /**
     * 通过博客id和父评论id查找所有子评论
     * @param blogId
     * @param parentCommentId
     * @return
     */
    List<PageComment> getPageCommentList(Long blogId, Long parentCommentId);

}
