package com.denghb.eorm.support.domain;

import lombok.Data;

/**
 * @author denghb
 * @since 2019-07-13 23:07
 */
@Data
public class ETrace {

    private String id;

    private Long startTime;

    private String logName;

    private String logMethod;

}
