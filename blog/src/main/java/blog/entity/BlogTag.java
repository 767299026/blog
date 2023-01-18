package blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 博客标签实体类
 *
 * @author fanfanli
 * @date 2021/4/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "blog_tag")
public class BlogTag implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long blogId;

    private Long tagId;


}
