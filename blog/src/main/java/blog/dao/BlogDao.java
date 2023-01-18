package blog.dao;

import blog.common.vo.BlogInfo;
import blog.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogDao extends BaseMapper<Blog> {

    //根据分类查询博客
    List<BlogInfo> getBlogByTypeName(String categoryName);
}
