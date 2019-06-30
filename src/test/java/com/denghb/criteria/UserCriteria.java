package com.denghb.criteria;

import com.denghb.eorm.domain.Paging;

/**
 * @author denghb 2019-06-30 18:05
 */
public class UserCriteria extends Paging {
    @Override
    public String[] getSorts() {
        return new String[] {"id"};
    }

    private String name;

    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
