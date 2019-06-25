package com.denghb.xxlibrary.test.service;

import com.denghb.eorm.page.EPageReq;
import com.denghb.eorm.page.EPageRes;
import com.denghb.xxlibrary.domain.Book;
import com.denghb.xxlibrary.service.BookService;
import com.denghb.xxlibrary.test.BaseTest;
import lombok.extern.log4j.Log4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author denghb 2019-06-25 00:19
 */
@Log4j
public class BookServiceTest extends BaseTest {

    private BookService bookService;

    @Before
    public void before2() {
        before();
        bookService = ctx.getBean(BookService.class);
    }

    @Test
    public void insert() {
        Book book = new Book();
        book.setTitle("test insert book");
        bookService.insert(book);
    }

    @Test
    public void save() {
        Book book = new Book();
        book.setTitle("test save book");
        bookService.save(book);
        book.setTitle("test save book3");
        bookService.save(book);
    }

    @Test
    public void updateById() {
        Book book = new Book();
        book.setTitle("test updateById book");
        bookService.insert(book);

        book.setTitle("test updateById book2");
        bookService.updateById(book);
    }

    @Test
    public void deletedById() {
        Book book = new Book();
        book.setTitle("test deletedById book");
        bookService.insert(book);

        bookService.deleteById(book.getId());
    }


    @Test
    public void selectById() {
        Book book = new Book();
        book.setTitle("test selectById book");
        bookService.insert(book);

        Book book2 = bookService.selectById(book.getId());
        log.info(book2);
    }

    @Test
    public void selectPage() {
        EPageReq req = new EPageReq();
        req.setPageSize(100);
        EPageRes<Book> res = bookService.selectPage(req);
        log.info(res);
    }
}