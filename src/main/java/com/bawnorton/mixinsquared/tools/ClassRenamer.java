package com.bawnorton.mixinsquared.tools;

import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class ClassRenamer {
    public static void renameClass(ClassNode classNode, String newClassName) {
        // 修改类名（注意：内部名称使用斜杠分隔符）
        String oldInternalName = classNode.name;
        String newInternalName = newClassName.replace('.', '/');
        classNode.name = newInternalName;

        // 修改字段的所属类名
        for (FieldNode field : classNode.fields) {
            field.desc = field.desc.replace(oldInternalName, newInternalName);
        }

        // 修改方法的所属类名和内部指令
        for (MethodNode method : classNode.methods) {
            // 修改方法的所属类名
            method.desc = method.desc.replace(oldInternalName, newInternalName);

            // 遍历方法内的所有指令，替换引用
            Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) insn;
                    if (fin.owner.equals(oldInternalName)) {
                        fin.owner = newInternalName;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    if (min.owner.equals(oldInternalName)) {
                        min.owner = newInternalName;
                    }
                } else if (insn instanceof TypeInsnNode) {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    tin.desc = tin.desc.replace(oldInternalName, newInternalName);
                }
            }
        }
    }
}