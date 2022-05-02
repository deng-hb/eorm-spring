package com.denghb.xxlibrary.edao;

import com.denghb.eorm.Edao;
import com.denghb.eorm.sql.Esql;
import com.denghb.xxlibrary.domain.ReadRecord;
import com.denghb.xxlibrary.domain.Student;

import java.util.List;

// succ
public interface StudentDao extends Edao<Student> {

    /**
     * 查询
     * @return
     */
    List<Student> selectYoung();
    String selectYoung = ""/*{
        select * from student where age < 18
    }*/;

    List<Student> list(Esql<Student> esql);

    List<ReadRecord> listR();
}
