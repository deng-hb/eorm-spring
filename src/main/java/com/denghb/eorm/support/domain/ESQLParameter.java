package com.denghb.eorm.support.domain;

import com.denghb.eorm.support.EKeyHolder;
import lombok.Data;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/24 20:46
 */
@Data
public class ESQLParameter {

    private String sql;

    private Object[] args = new Object[0];

    private EKeyHolder keyHolder;
}
