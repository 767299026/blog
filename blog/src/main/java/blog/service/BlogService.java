package blog.service;

import blog.common.vo.BlogInfo;
import blog.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlogService extends IService<Blog> {

    /**
     * 通过分类名查找属于该分类的博客list
     *
     * @param categoryName
     * @return
     */
    List<BlogInfo> getBlogInfoListByCategoryName(String categoryName);
}
