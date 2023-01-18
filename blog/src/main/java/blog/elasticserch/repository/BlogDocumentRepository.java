package blog.elasticserch.repository;

import blog.elasticserch.document.BlogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BlogDocumentRepository extends ElasticsearchRepository<BlogDocument,Long> {


}
