package com.newsapps.newstamil;

import com.newsapps.newstamil.types.Article;
import com.newsapps.newstamil.types.ArticleList;

public interface HeadlinesEventListener {
	void onArticleListSelectionChange(ArticleList m_selectedArticles);
	void onArticleSelected(Article article);
	void onArticleSelected(Article article, boolean open);
	void onHeadlinesLoaded(boolean appended);	
}
