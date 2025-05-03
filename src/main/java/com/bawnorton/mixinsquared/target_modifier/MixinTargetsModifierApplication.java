package com.bawnorton.mixinsquared.target_modifier;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.type.RemappableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinTargetModifier;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import com.bawnorton.mixinsquared.reflection.FieldReference;
import com.bawnorton.mixinsquared.reflection.MixinTransformerExtension;
import com.bawnorton.mixinsquared.tools.ClassRenamer;
import com.llamalad7.mixinextras.utils.ClassGenUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class MixinTargetsModifierApplication {
    static final ILogger LOGGER = MixinService.getService().getLogger("mixinsquared-target-modifier");
    static MixinTargetsModifierApplication INSTANCE;
    private static final FieldReference<String> pluginClassName;
    private static final FieldReference<IMixinService> mixinService;

    static {
        try {
            Class<?> mixinConfigClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
            pluginClassName = new FieldReference<>(mixinConfigClass, "pluginClassName");
            mixinService = new FieldReference<>(mixinConfigClass, "service");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * key: original mixin class name, value: target modifier
     */
    static final Map<String, MixinTargetModifier> MODIFIERS = new HashMap<>();
    final Map<String, String> generatedToOriginalMixins = new HashMap<>();
    final Set<String> originalMixins = new HashSet<>();
    final MethodHandles.Lookup lookup;
    final IMixinConfigPlugin mixinSquaredPlugin;
    private final String generatedMixinPrefix;

    public static void init(MethodHandles.Lookup lookup, IMixinConfigPlugin mixinSquaredPlugin) {
        if (INSTANCE != null) {
            throw new IllegalStateException("TargetModifierApplication is already initialized");
        }
        INSTANCE = new MixinTargetsModifierApplication(lookup, mixinSquaredPlugin);
    }

    public static MixinTargetsModifierApplication getInstance() {
        return INSTANCE;
    }

    private MixinTargetsModifierApplication(MethodHandles.Lookup lookup, IMixinConfigPlugin mixinSquaredPlugin) {
        MixinCancellerRegistrar.register((targetClassName, mixinClassName) -> originalMixins.contains(mixinClassName));
        this.lookup = lookup;
        this.mixinSquaredPlugin = mixinSquaredPlugin;
        this.generatedMixinPrefix = lookup.lookupClass().getPackage().getName() +
                                    ".MixinSquaredGenerated$";
    }

    public List<String> applyModifiers() {
        IMixinTransformer activeTransformer =
            (IMixinTransformer) MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
        // FIXME: this is unsafe
        List<IMixinConfig> pendingConfigs = MixinTransformerExtension.tryAs(activeTransformer)
            .map(MixinTransformerExtension::getPendingConfigs)
            .orElseThrow(() -> new UnsupportedOperationException("Unsupported mixin transformer: " + activeTransformer.getClass()));
        // Find our mixin config
        IMixinConfig mixinConfig = null;
        String pluginClass = mixinSquaredPlugin.getClass().getName();
        for (IMixinConfig config : pendingConfigs) {
            String aPlugin = pluginClassName.get(config);
            if (pluginClass.equals(aPlugin)) {
                mixinConfig = config;
                break;
            }
        }
        assert mixinConfig != null;
        // Exchange mixin service with our own wrapper so that we can modify target classes
        IMixinService service = mixinService.get(mixinConfig);
        MixinServiceWrapper mixinServiceWrapper;
        if (!(service instanceof MixinServiceWrapper)) {
            LOGGER.info("Wrapping mixin service for {} so that we can modify target classes.", mixinConfig);
            mixinServiceWrapper = new MixinServiceWrapper(service);
            mixinService.set(mixinConfig, mixinServiceWrapper);
        } else {
            mixinServiceWrapper = (MixinServiceWrapper) service;
        }
        IClassBytecodeProvider bytecodeProvider = mixinServiceWrapper.getBytecodeProvider();
        // Apply modifiers!
        for (MixinTargetModifier modifier : MODIFIERS.values()) {
            applyModifier(bytecodeProvider, modifier);
        }
        // Collect generated mixin class's simple names
        List<String> list = new ArrayList<>(generatedToOriginalMixins.size());
        for (String s : generatedToOriginalMixins.keySet()) {
            String substring = s.substring(s.lastIndexOf('.') + 1);
            list.add(substring);
        }
        return list;
    }

    public boolean shouldApplyMixin(String targetClassName, String generatedMixinClassName) {
        String mixinClassName = generatedToOriginalMixins.get(generatedMixinClassName);
        return MODIFIERS.get(mixinClassName).shouldApplyMixin(targetClassName);
    }

    private void applyModifier(IClassBytecodeProvider bytecodeProvider, MixinTargetModifier modifier) {
        String mixinClassName = modifier.getMixinClassName();
        // Get the original mixin class node
        ClassNode cNode;
        try {
            cNode = bytecodeProvider.getClassNode(mixinClassName);
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        // A list of original target classes
        List<String> targets = new ArrayList<>();
        AnnotationNode aNode = Annotations.getInvisible(cNode, Mixin.class);
        List<Object> values = aNode.values;
        // The index of the "value" key in the annotation
        int valueIndex = -1;
        for (ListIterator<Object> iterator = values.listIterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            Object value = iterator.next();
            if ("value".equals(key)) {
                @SuppressWarnings("unchecked")
                List<Type> classes = (List<Type>) value;
                if (classes.isEmpty()) {
                    continue;
                }
                for (Type c : classes) {
                    targets.add(c.getClassName());
                }
                valueIndex = iterator.previousIndex();
                // We'll overwrite the original Class type targets later
            } else if ("targets".equals(key)) {
                @SuppressWarnings("unchecked")
                List<String> originalTargets = (List<String>) value;
                if (originalTargets.isEmpty()) {
                    continue;
                }
                // remove original class targets
                targets.addAll(originalTargets);
                iterator.set(Collections.emptyList());
            }
        }
        List<String> unmodifiableList = Collections.unmodifiableList(targets);
        // Modify the target classes
        List<String> mixins = modifier.getTargets(unmodifiableList);
        if (mixins == null || mixins == unmodifiableList) {
            return;
        }
        // Remove the original Class type targets
        // and write the modified to the annotation
        ArrayList<Type> targetTypes = new ArrayList<>(mixins.size());
        mixins.forEach(target -> targetTypes.add(Type.getObjectType(target.replace('.', '/'))));
        if (valueIndex > -1) {
            values.set(valueIndex, targetTypes);
        } else {
            values.add("value");
            values.add(targetTypes);
        }
        for (MethodNode mNode : cNode.methods) {
            if (mNode.visibleAnnotations == null) {
                continue;
            }
            for (AnnotationNode aNode1 : mNode.visibleAnnotations) {
                AdjustableAnnotationNode aaNode = AdjustableAnnotationNode.fromNode(aNode1);
                if (aaNode instanceof RemappableAnnotationNode) {
                    RemappableAnnotationNode raNode = (RemappableAnnotationNode) aaNode;
                    // Apply refmap to method targets
                    raNode.applyRefmap();
                    // MUST remove the class name from the method targets
                    List<String> ss = new ArrayList<>(aaNode.<List<String>>get("method").get());
                    for (ListIterator<String> iterator = ss.listIterator(); iterator.hasNext(); ) {
                        String s1 = iterator.next();
                        if (s1.indexOf('(') > s1.indexOf(';')) {
                            iterator.set(s1.substring(s1.indexOf(';') + 1));
                        }
                    }
                    aaNode.set("method", ss);
                }
            }
        }
        // Rename the modified mixin class to a generated name
        String generatedMixin = generatedMixinPrefix +
                                mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        ClassRenamer.renameClass(cNode, generatedMixin);
        // Define the modified mixin class
        // TODO: Change the usage of ClassGenUtils to our implementation.
        ClassGenUtils.defineClass(cNode, lookup);
        // Done!
        generatedToOriginalMixins.put(generatedMixin, mixinClassName);
        originalMixins.add(mixinClassName);
    }
}
