package com.newsapps.newstamil.types;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class ArticleList extends ArrayList<Article> implements Parcelable {
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeList(this);
	}

    public boolean containsId(int id) {
        return findById(id) != null;
    }

    public boolean contains(Article article) {
        return containsId(article.id);
    }

	public Article findById(int id) {
		for (Article a : this) {
			if (a.id == id)
				return a;			
		}			
		return null;
	}
	
	public void readFromParcel(Parcel in) {
		in.readList(this, getClass().getClassLoader());
	}
	
	public ArticleList() { }
	
	public ArticleList(Parcel in) {		
		readFromParcel(in);
	}

	@SuppressWarnings("rawtypes")
	public static final Creator CREATOR =
    	new Creator() {
            public ArticleList createFromParcel(Parcel in) {
                return new ArticleList(in);
            }
 
            public ArticleList[] newArray(int size) {
                return new ArticleList[size];
            }
        };
}