package blog.service;

import blog.common.vo.UserInfo;
import blog.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserService extends IService<User>{

    /**
     * 查询所有用户（只含有部分信息）
     * @return 用户（只含有部分信息）list
     */
    List<UserInfo> getUserInfoList();
}
