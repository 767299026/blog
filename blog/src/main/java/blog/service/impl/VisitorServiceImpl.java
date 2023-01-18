package blog.service.impl;

import blog.dao.VisitorDao;
import blog.entity.Visitor;
import blog.service.VisitorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@SuppressWarnings("all")
@Service
@Transactional
public class VisitorServiceImpl extends ServiceImpl<VisitorDao, Visitor> implements VisitorService {

    @Resource
    private VisitorDao visitorDao;

    /**
     * 通过uuid查询是否存在是该uuid的访客
     * @param uuid
     * @return
     */
    @Override
    public boolean hasUUID(String uuid) {
        return visitorDao.hasUUID(uuid) != 0;
    }

    /**
     * 通过uuid查询访客
     * @param uuid
     * @return
     */
    @Override
    public Visitor getVisitorByUuid(String uuid){
        return visitorDao.selectByUuid(uuid);
    }

    /**
     * 获取Pv
     * @return pv
     */
    @Override
    public int getPv(){
        return  visitorDao.getPv();
    }
}
