package blog.dao;

import blog.common.vo.PageComment;
import blog.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentDao extends BaseMapper<Comment> {

    List<PageComment> getPageCommentListByPageAndParentCommentIdByDesc(Long blogId, Long parentCommentId);

    List<PageComment> getPageCommentListByPageAndParentCommentId(Long blogId, Long parentCommentId);
}
