/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.xxlibrary.model;

import com.denghb.xxlibrary.domain.Book;
import com.denghb.xxlibrary.domain.ReadRecord;
import com.denghb.xxlibrary.domain.Student;
import lombok.Data;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/07/17 22:21
 */
@Data
public class ReadRecordModel {

    private ReadRecord readRecord;

    private Book book;

    private Student student;
}
