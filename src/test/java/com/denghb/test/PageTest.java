package com.denghb.test;

import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.test.domain.User;
import org.junit.Test;

/**
 * @author denghb
 * @since 2019-07-14 17:58
 */
public class PageTest extends AppTest {

    @Test
    public void test1() {

        String sql = ""/*{
            select * from tb_user where deleted = 0
        }*/;

        Paging paging = new Paging() {
            @Override
            public String[] getSorts() {
                return new String[]{"id"};
            }
        };

        PagingResult<User> page = db.page(User.class, new StringBuffer(sql), paging);

        log.info(page);
    }
}
