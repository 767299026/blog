package blog.dao;

import blog.common.vo.UserInfo;
import blog.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDao extends BaseMapper<User> {


    List<UserInfo> getUserInfo();
}
