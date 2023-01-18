package blog.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 友链实体类
 *
 * @author fanfanli
 * @date  2021/4/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Friend implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 描述
     */
    private String description;

    /**
     * 站点
     */
    private String website;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 公开或隐藏
     */
    private Boolean isPublished;

    /**
     * 点击次数
     */
    private Integer views;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
