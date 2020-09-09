/* Copyright © 2020 meihuasoft.com All rights reserved. */
package com.denghb.eorm.support.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请输入描述信息
 *
 * @author denghongbing
 * @date 2020/09/09 12:54
 */
@Data
public class SelectSQL {

    // 字段名
    private List<String> fields = new ArrayList<>();

    // 别名,表名（或者是临时表）
    private Map<String, String> table = new HashMap<>();

}
