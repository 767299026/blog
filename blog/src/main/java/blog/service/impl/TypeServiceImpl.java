package blog.service.impl;

import blog.dao.TypeDao;
import blog.entity.Type;
import blog.service.TypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TypeServiceImpl extends ServiceImpl<TypeDao, Type> implements TypeService {

}
