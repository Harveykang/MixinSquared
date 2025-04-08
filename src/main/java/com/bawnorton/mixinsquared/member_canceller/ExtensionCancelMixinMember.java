package com.bawnorton.mixinsquared.member_canceller;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinMemberCanceller;
import com.bawnorton.mixinsquared.reflection.FieldReference;
import com.bawnorton.mixinsquared.reflection.TargetClassContextExtension;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.service.MixinService;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ExtensionCancelMixinMember implements IExtension {
    static final ILogger LOGGER = MixinService.getService().getLogger("mixinsquared:node_canceller");
    private static final String SHADOW_DESC = Type.getDescriptor(Shadow.class);
    static final List<MixinMemberCanceller> CANCELLERS = new CopyOnWriteArrayList<>();
    // from MixinExtras com.llamalad7.mixinextras.transformer.MixinTransformerExtension
    private final Set<ClassNode> preparedMixins = Collections.newSetFromMap(new WeakHashMap<>());
    private final FieldReference<Object> field_MixinInfo$state;
    private final FieldReference<ClassNode> field_MixinInfo$State$classNode;

    public ExtensionCancelMixinMember() {
        try {
            Class<?> infoClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
            field_MixinInfo$state = new FieldReference<>(infoClass, "state");
            Class<?> stateClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo$State");
            field_MixinInfo$State$classNode = new FieldReference<>(stateClass, "classNode");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {
        if (CANCELLERS.isEmpty()) {
            return;
        }
        Optional<TargetClassContextExtension> optional = TargetClassContextExtension.tryAs(context);
        if (!optional.isPresent()) {
            return;
        }
        TargetClassContextExtension contextExtension = optional.get();
        SortedSet<IMixinInfo> mixins = contextExtension.getMixins();
        for (IMixinInfo mixin : mixins) {
            // Get the internal class node of the mixinInfo.
            ClassNode cNode = field_MixinInfo$State$classNode.get(field_MixinInfo$state.get(mixin));
            if (preparedMixins.contains(cNode)) {
                continue;
            }

            String mixinClassName = mixin.getClassName();
            List<String> l = mixin.getTargetClasses();
            List<String> targetClassNames = new ArrayList<>(l.size());
            for (String s : l) {
                String string = s.replaceAll("/", ".");
                targetClassNames.add(string);
            }

            // Filter out cancellers that don't applied to the given mixin class.
            List<MixinMemberCanceller> cancellers = null;
            for (MixinMemberCanceller canceller : CANCELLERS) {
                if (canceller.preCancel(targetClassNames, mixinClassName)) {
                    if (cancellers == null) {
                        cancellers = new ArrayList<>(2);
                    }
                    cancellers.add(canceller);
                }
            }

            if (cancellers != null) {
                List<MethodNode> methods = cNode.methods;
                if (methods != null && !methods.isEmpty()) {
                    cancelMethod(targetClassNames, mixinClassName, cancellers, methods);
                }
                List<FieldNode> fields = cNode.fields;
                if (fields != null && !fields.isEmpty()) {
                    // Assert all accesses from mixin methods to the cancelled fields are removed.
                    cancelField(targetClassNames, mixinClassName, cancellers, fields, cNode);
                }
            }

            preparedMixins.add(cNode);
        }
    }

    private void cancelMethod(List<String> targetClassNames,
                              String mixinClassName,
                              List<MixinMemberCanceller> cancellers,
                              List<MethodNode> methods) {
        Iterator<MethodNode> iterator = methods.iterator();
        while (iterator.hasNext()) {
            MethodNode mNode = iterator.next();
            List<String> targetMethodNames;

            VisibleAnnotations:
            if (mNode.visibleAnnotations == null || mNode.visibleAnnotations.isEmpty()) {
                targetMethodNames = new ArrayList<>();
            } else {
                for (AnnotationNode aNode : mNode.visibleAnnotations) {
                    AdjustableAnnotationNode aaNode = AdjustableAnnotationNode.fromNode(aNode);
                    // TODO: Make it faster. Maybe don't need an AdjustableAnnotationNode.
                    Optional<List<String>> opt = aaNode.get("method");
                    if (!opt.isPresent()) {
                        continue;
                    }
                    List<String> methodValue = opt.get();
                    // Copy to ensure the type is ArrayList (not Arrays$ArrayList).
                    targetMethodNames = new ArrayList<>(methodValue);
                    break VisibleAnnotations;
                }
                targetMethodNames = new ArrayList<>();
            }

            List<String> parameterNames;
            if (mNode.parameters == null || mNode.parameters.isEmpty()) {
                parameterNames = new ArrayList<>();
            } else {
                parameterNames = new ArrayList<>(mNode.parameters.size());
                for (ParameterNode parameter : mNode.parameters) {
                    parameterNames.add(parameter.name);
                }
            }

            for (MixinMemberCanceller canceller : cancellers) {
                boolean b = canceller.shouldCancelMethod(targetClassNames,
                    mixinClassName,
                    targetMethodNames,
                    mNode.name,
                    mNode.desc,
                    parameterNames);
                if (b) {
                    iterator.remove();
                    LOGGER.warn("Cancelled mixin method {}#{} by {}", mixinClassName, mNode.desc, canceller.getClass().getName());
                    break;
                }
            }
        }
    }

    private void cancelField(List<String> targetClassNames,
                             String mixinClassName,
                             List<MixinMemberCanceller> cancellers,
                             List<FieldNode> fields,
                             ClassNode classNode) {
        Iterator<FieldNode> iterator = fields.iterator();
        Set<String> removed = null;
        Root:
        while (iterator.hasNext()) {
            FieldNode field = iterator.next();
            // If the field
            if (field.visibleAnnotations != null && !field.visibleAnnotations.isEmpty()) {
                for (AnnotationNode aNode : field.visibleAnnotations) {
                    if (SHADOW_DESC.equals(aNode.desc)) {
                        continue Root;
                    }
                }
            }

            for (MixinMemberCanceller canceller : cancellers) {
                boolean b = canceller.shouldCancelField(targetClassNames,
                    mixinClassName,
                    field.name,
                    field.desc);
                if (b) {
                    iterator.remove();
                    if (removed == null) {
                        removed = new HashSet<>();
                    }
                    removed.add(field.name);
                    LOGGER.warn("Cancelled mixin field {}#{} by {}", mixinClassName, field.desc, canceller.getClass().getName());
                    break;
                }
            }
        }

        if (removed == null) {
            return;
        }

        for (MethodNode mNode : classNode.methods) {
            if (!"<clinit>".equals(mNode.name) &&
                !"<init>".equals(mNode.name)) {
                continue;
            }
            ListIterator<AbstractInsnNode> iterator1 = mNode.instructions.iterator();
            while (iterator1.hasNext()) {
                AbstractInsnNode iNode = iterator1.next();
                if (!(iNode instanceof FieldInsnNode)) {
                    continue;
                }
                FieldInsnNode fieldInsnNode = (FieldInsnNode) iNode;
                if (removed.contains(fieldInsnNode.name)) {
                    int opcode = fieldInsnNode.getOpcode();
                    if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
                        throw new InvalidMixinMemberCancellerException("Cannot cancel field access in static initializer or constructor of " + mixinClassName + "#" + mNode.name + mNode.desc);
                    }
                    iterator1.remove();
                }
            }
        }
    }

    @Override
    public void postApply(ITargetClassContext context) {
    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
    }

}
