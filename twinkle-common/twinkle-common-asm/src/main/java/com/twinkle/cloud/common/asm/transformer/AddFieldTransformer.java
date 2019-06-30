package com.twinkle.cloud.common.asm.transformer;

import com.twinkle.cloud.common.asm.data.AnnotationDefine;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2019-06-26 21:50<br/>
 *
 * @author chenxj
 * @see
 * @since JDK 1.8
 */
@Slf4j
public class AddFieldTransformer extends ClassTransformer {
    private int fieldAccess;
    private String fieldName;
    private String fieldDesc;
    private List<AnnotationDefine> annotationDefineList;

    public AddFieldTransformer(ClassTransformer _transformer, int _fieldAccess,
                               String _fieldName, String _fieldDesc, List<AnnotationDefine> _annotationDefineList) {
        super(_transformer);
        this.fieldAccess = _fieldAccess;
        this.fieldName = _fieldName;
        this.fieldDesc = _fieldDesc;
        this.annotationDefineList = _annotationDefineList;
    }

    @Override
    public void transform(ClassNode _classNode) {
        boolean isPresent = false;
        for (FieldNode tempFieldNode : _classNode.fields) {
            if (this.fieldName.equals(tempFieldNode.name)) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            FieldNode tempFieldNode = new FieldNode(fieldAccess, fieldName, fieldDesc, null, null);
            tempFieldNode.visibleAnnotations.addAll(this.getAnnotationNode());
            _classNode.fields.add(tempFieldNode);
        }
        super.transform(_classNode);
    }

    /**
     * Going to Pack the Annotation Node List.
     *
     * @return
     */
    private List<AnnotationNode> getAnnotationNode(){
        if(CollectionUtils.isEmpty(this.annotationDefineList)) {
            return new ArrayList<>();
        }
        return this.annotationDefineList.stream().map(this::packAnnotationNode).collect(Collectors.toList());
    }

    /**
     * Pack the Annotation Node.
     *
     * @param _define
     * @return
     */
    private AnnotationNode packAnnotationNode(AnnotationDefine _define){
        log.debug("Going to add field's annotation {}", _define);
        AnnotationNode tempNode = new AnnotationNode(_define.getAnnotationClass().getCanonicalName());
        Map<String, Object> tempItemMap = _define.getValuesMap();
        if(CollectionUtils.isEmpty(tempItemMap)) {
            return tempNode;
        }
        tempItemMap.forEach((k, v) -> {
            tempNode.visit(k, v);
        });
        return tempNode;
    }
}
