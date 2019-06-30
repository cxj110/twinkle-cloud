package com.twinkle.cloud.common.asm.data;

import lombok.Data;

import java.util.Map;

/**
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2019-06-26 16:01<br/>
 *
 * @author chenxj
 * @see
 * @since JDK 1.8
 */
@Data
public class AnnotationDefine {
    private Class<?> annotationClass;
    private Map<String, Object> valuesMap;
}
