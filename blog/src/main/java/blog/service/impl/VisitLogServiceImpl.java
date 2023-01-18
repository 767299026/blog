package blog.service.impl;

import blog.dao.VisitLogDao;
import blog.entity.VisitLog;
import blog.service.VisitLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class VisitLogServiceImpl extends ServiceImpl<VisitLogDao,VisitLog> implements VisitLogService {

}
