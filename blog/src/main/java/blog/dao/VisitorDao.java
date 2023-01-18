package blog.dao;

import blog.entity.Visitor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@SuppressWarnings("all")
@Repository
public interface VisitorDao extends BaseMapper<Visitor> {

    /**
     * 查询uuid是否已经存在
     * @param uuid
     * @return 0为不存在，1为存在
     */
    int hasUUID(String uuid);

    /**
     * 通过uuid找到访客
     * @param uuid
     * @return 返回访客对象
     */
    Visitor selectByUuid(@Param("uuid") String uuid);

    /**
     * 计算pv
     * @return
     */
    int getPv();
}
