package blog.common.dto;

import lombok.Data;

/**
 * 用于统计文章归档的对象
 * 用于数据库查询 无须持久化
 */
@Data
public class Archives {

    private Integer year;

    private Integer month;

    private Long count;
}
