package blog.elasticserch.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Document(indexName = "yiqu_blog",createIndex = true)
public class BlogDocument {

    @Id
    private Long id;
    @Field(type = FieldType.Text,searchAnalyzer = "ik_smart",analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Text)
    private String firstPicture;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Text,searchAnalyzer = "ik_smart",analyzer = "ik_max_word")
    private String content;
    @Field(type = FieldType.Integer)
    private Integer views;
    @Field(type = FieldType.Integer)
    private Integer words;
    @Field(type = FieldType.Long)
    private Long typeId;
    @Field(type = FieldType.Long)
    private Long userId;
    @Field(type = FieldType.Integer)
    private Integer status;
}
