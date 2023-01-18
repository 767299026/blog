package blog.service.impl;

import blog.common.vo.BlogInfo;
import blog.dao.BlogDao;
import blog.entity.Blog;
import blog.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class BlogServiceImpl extends ServiceImpl<BlogDao, Blog> implements BlogService {

    @Resource
    private BlogDao blogDao;

    @Override
    public List<BlogInfo> getBlogInfoListByCategoryName(String categoryName) {
        return blogDao.getBlogByTypeName(categoryName);
    }
}
