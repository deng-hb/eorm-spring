package com.denghb.xxlibrary.test.service;

import com.denghb.xxlibrary.domain.Book;
import com.denghb.xxlibrary.service.BookService;
import com.denghb.xxlibrary.test.BaseTest;
import org.junit.Test;

/**
 * @author denghb 2019-06-25 00:19
 */
public class BookServiceTest extends BaseTest {


    @Test
    public void save() {
        Book book = new Book();
        book.setTitle("test book");
        ctx.getBean(BookService.class).save(book);
    }
}