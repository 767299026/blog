package blog.service.impl;

import blog.dao.FriendDao;
import blog.entity.Friend;
import blog.service.FriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FriendServiceImpl extends ServiceImpl<FriendDao, Friend> implements FriendService {

}
