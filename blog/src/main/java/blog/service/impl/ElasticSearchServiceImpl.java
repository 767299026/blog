package blog.service.impl;

import blog.elasticserch.document.BlogDocument;
import blog.service.ElasticSearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public List<BlogDocument> searchBlogByTitle(String queryString) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        NativeSearchQuery query = queryBuilder
                .withFilter(QueryBuilders.termQuery("status",1))
                .withQuery(QueryBuilders.multiMatchQuery(queryString,"title","content").analyzer("ik_smart"))
                .build();
        SearchHits<BlogDocument> searchHits = elasticsearchRestTemplate.search(query, BlogDocument.class);
        return searchHits
                .get()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }
}
