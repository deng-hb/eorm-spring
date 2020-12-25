package com.denghb.eorm.support;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * FIXME 简单介绍该类
 *
 * @author denghongbing
 * @date 2020/12/25 18:24
 */
@FunctionalInterface
public interface EBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {

}
