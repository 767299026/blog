package blog.dao;

import blog.entity.VisitLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitLogDao extends BaseMapper<VisitLog> {
}
