package blog.service.impl;

import blog.common.vo.PageComment;
import blog.dao.CommentDao;
import blog.entity.Comment;
import blog.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService {

    @Resource
    private CommentDao commentDao;

    /**
     * 通过博客id和父评论id查找所有子评论 并按照时间倒序排序
     *
     * @param blogId
     * @param parentCommentId
     * @return
     */
    @Override
    public List<PageComment> getPageCommentListByDesc(Long blogId, Long parentCommentId) {
        return commentDao.getPageCommentListByPageAndParentCommentIdByDesc(blogId, parentCommentId);
    }

    /**
     * 通过博客id和父评论id查找所有子评论
     *
     * @param blogId
     * @param parentCommentId
     * @return
     */
    @Override
    public List<PageComment> getPageCommentList(Long blogId, Long parentCommentId) {
        return commentDao.getPageCommentListByPageAndParentCommentId(blogId, parentCommentId);
    }
}
