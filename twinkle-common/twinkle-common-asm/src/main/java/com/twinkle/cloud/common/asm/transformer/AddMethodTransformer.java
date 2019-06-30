package com.twinkle.cloud.common.asm.transformer;

import com.twinkle.cloud.common.asm.data.AnnotationDefine;
import com.twinkle.cloud.common.asm.data.MethodDefine;
import com.twinkle.cloud.common.asm.data.ParameterDefine;
import com.twinkle.cloud.common.asm.utils.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2019-06-27 18:31<br/>
 *
 * @author chenxj
 * @see
 * @since JDK 1.8
 */
@Slf4j
public abstract class AddMethodTransformer extends ClassTransformer {
    /**
     * Class Transformer.
     */
    protected ClassTransformer classTransformer;
    /**
     * Method definition, will be used to add into the dest class.
     */
    protected MethodDefine methodDefine;
    /**
     * The packed Method node.
     */
    protected MethodNode methodNode;
    /**
     * The Label List for this method.
     */
    protected List<Label> labelList;
    /**
     * The class node.
     */
    protected ClassNode classNode;

    public AddMethodTransformer(ClassTransformer _transformer, MethodDefine _methodDefine){
        super(_transformer);
        this.classTransformer = _transformer;
        this.methodDefine = _methodDefine;
        this.labelList = new ArrayList<>();
    }

    @Override
    public void transform(ClassNode _classNode) {
        this.classNode = _classNode;
        boolean isPresent = false;
        for(MethodNode tempNode : _classNode.methods) {
            if(!this.methodDefine.getName().equals(tempNode.name)) {
                continue;
            }
            // Descriptor is different, so dismiss.
            if(!this.methodDefine.getDescriptor().equals(tempNode.desc)) {
                continue;
            }
            //Signature is different, so dismiss.
            if(!this.methodDefine.getSignature().equals(tempNode.signature)) {
                continue;
            }
            log.info("Find the same method [{}] with same parameters in this class.", this.methodDefine.getName());
            isPresent = true;
        }

        if (!isPresent) {
            this.methodNode = this.packMethodNode();
            this.visitMethodAnnotations();
            this.visitMethodParameters();
            this.visitLocalParameters();
        }
    }

    /**
     * Pack the instructions for this method.
     *
     * @return
     */
    public abstract MethodNode packMethodNode();

    /**
     * Visit the Method's Annotations.
     */
    private void visitMethodAnnotations() {
        if (CollectionUtils.isEmpty(this.methodDefine.getAnnotationDefineList())) {
            log.debug("Do not find Method[{}]'s Annotations definitions.", this.methodDefine.getName());
            return;
        }
        for(AnnotationDefine tempDefine : this.methodDefine.getAnnotationDefineList()) {
            AnnotationVisitor tempVisitor = this.methodNode.visitAnnotation(
                    Type.getDescriptor(tempDefine.getAnnotationClass()),
                    true
            );

            if (CollectionUtils.isEmpty(tempDefine.getValuesMap())) {
                log.debug("Do not find Method[{}]'s Annotation[{}] values.", this.methodDefine.getName(), tempDefine.getAnnotationClass());
                continue;
            }
            //Add Annotation's Parameters and the mapping Values.
            tempDefine.getValuesMap().forEach(
                    (k, v) -> tempVisitor.visit(k, v));
        }
    }

    /**
     * Visit the method's parameters.
     */
    private void visitMethodParameters() {
        if (CollectionUtils.isEmpty(this.methodDefine.getParameterDefineList())) {
            return;
        }
        int tempIndex = 0;
        for (ParameterDefine tempDefine : this.methodDefine.getParameterDefineList()) {
            this.methodNode.visitParameter(
                    tempDefine.getName(),
                    tempDefine.getAccess()
            );
            //Update the Parameter Index.
            tempIndex ++;

            List<AnnotationDefine> tempAnnotationDefineList = tempDefine.getAnnotationDefineList();
            if (CollectionUtils.isEmpty(tempAnnotationDefineList)) {
                log.debug("Do not find Method[{}]-Parameter[{}]-Annotations.", this.methodDefine.getName(), tempDefine.getName());
                continue;
            }

            for (AnnotationDefine tempAnnotationDefine : tempAnnotationDefineList) {
                AnnotationVisitor tempVisitor = this.methodNode.visitParameterAnnotation(
                        tempIndex,
                        Type.getDescriptor(tempAnnotationDefine.getAnnotationClass()),
                        true
                );
                if (CollectionUtils.isEmpty(tempAnnotationDefine.getValuesMap())) {
                    log.debug("Do not find Method[{}]-Parameter[{}]-Annotation[{}]'s values.", this.methodDefine.getName(), tempDefine.getName(), tempAnnotationDefine.getAnnotationClass());
                    continue;
                }
                //Add Annotation's Parameters and the mapping Values.
                tempAnnotationDefine.getValuesMap().forEach(
                        (k, v) -> tempVisitor.visit(k, v));
            }
        }
    }

    /**
     * Visit all the local Parameters.
     */
    private void visitLocalParameters() {
        // "this" will be initialized as 0 index all times.
        this.methodNode.visitLocalVariable(
                "this",
                this.classNode.name,
                this.classNode.signature,
                this.getLabelFromLabelNode(this.methodNode.instructions.getFirst()),
                this.getLabelFromLabelNode(this.methodNode.instructions.getLast()),
                0
        );
        log.debug("Added [this] parameter.");
        //Visit the Method parameter firstly.
        //The parameters of the method are the local ones as well.
        this.visitLocalParameterList(this.methodDefine.getParameterDefineList(), 1);
        //Visit the Method's Local Parameters.
        int tempIndex = this.methodNode.localVariables.size();
        this.visitLocalParameterList(this.methodDefine.getLocalParameterDefineList(), tempIndex);
    }

    /**
     * Visit the local parameters.
     *
     * @param _defineList
     * @param _startIndex
     */
    private void visitLocalParameterList(List<ParameterDefine> _defineList, int _startIndex) {
        if (CollectionUtils.isEmpty(_defineList)) {
            return;
        }
        int tempIndex = _startIndex;
        for (ParameterDefine tempItem : _defineList) {
            this.methodNode.visitLocalVariable(
                    tempItem.getName(),
                    Type.getDescriptor(tempItem.getTypeDefine().getTypeClass()),
                    TypeUtil.getTypeSignature(tempItem.getTypeDefine()),
                    this.getLabelFromList(tempItem.getStartLabelIndex()),
                    this.getLabelFromList(tempItem.getEndLabelIndex()),
                    tempIndex++
            );
        }
    }

    /**
     * Get the Label from LabelNode.
     *
     * @param _insnNode
     * @return
     */
    private Label getLabelFromLabelNode(AbstractInsnNode _insnNode) {
        if (_insnNode instanceof LabelNode) {
            return ((LabelNode) _insnNode).getLabel();
        }
        log.warn("The node [{}] is not LabelNode, so return a new Label.", _insnNode);
        return new Label();
    }

    /**
     * Get label by the label's index.
     *
     * @param _index
     * @return
     */
    private Label getLabelFromList(int _index) {
        if (CollectionUtils.isEmpty(this.labelList)) {
            log.warn("The Label list is empty, so return a new Label.");
            return new Label();
        }
        if (_index >= this.labelList.size()) {
            log.warn("The index [{}] exceed the size of the label list, so return a new Label.", _index);
            return new Label();
        }
        return this.labelList.get(_index);
    }
}
