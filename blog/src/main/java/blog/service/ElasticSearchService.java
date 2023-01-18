package blog.service;

import blog.elasticserch.document.BlogDocument;

import java.util.List;

public interface ElasticSearchService {

    List<BlogDocument> searchBlogByTitle(String queryString);
}
