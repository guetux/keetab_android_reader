package com.keetab.library;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;

public class BookHelper {

	
	public static String getAuthors(Book epub) {
		StringBuilder sb = new StringBuilder();
		for (Author author : epub.getMetadata().getAuthors()) {
			sb.append(author.toString());
			sb.append(" | ");
		}
		sb.delete(sb.length()-2, sb.length()-1);
		return sb.toString();
	}
}
