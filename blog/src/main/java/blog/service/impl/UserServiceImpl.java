package blog.service.impl;

import blog.common.vo.UserInfo;
import blog.dao.UserDao;
import blog.entity.User;
import blog.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public List<UserInfo> getUserInfoList() {
        return userDao.getUserInfo();
    }
}
