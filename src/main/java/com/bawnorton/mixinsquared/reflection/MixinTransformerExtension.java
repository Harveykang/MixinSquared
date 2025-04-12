package com.bawnorton.mixinsquared.reflection;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MixinTransformerExtension {
    private final IMixinTransformer reference;
    private static FieldReference<List<IMixinConfig>> pendingConfigs;
    private static FieldReference<?> mixinProcessor;

    private MixinTransformerExtension(IMixinTransformer reference) {
        this.reference = reference;
        prepareReflections();
    }

    private static void prepareReflections() {
        if (mixinProcessor == null) {
            Class<?> mixinTransformerClass;
            try {
                mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            mixinProcessor = new FieldReference<>(mixinTransformerClass, "processor");
        }
        if (pendingConfigs == null) {
            Class<?> mixinProcessorClass;
            try {
                mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            pendingConfigs = new FieldReference<>(mixinProcessorClass, "pendingConfigs");
        }
    }

    public static void tryAs(IMixinTransformer reference, Consumer<MixinTransformerExtension> consumer) {
        if (reference.getClass().getName().equals("org.spongepowered.asm.mixin.transformer.MixinTransformer")) {
            consumer.accept(new MixinTransformerExtension(reference));
        }
    }

    public static Optional<MixinTransformerExtension> tryAs(IMixinTransformer reference) {
        if (reference.getClass().getName().equals("org.spongepowered.asm.mixin.transformer.MixinTransformer")) {
            return Optional.of(new MixinTransformerExtension(reference));
        } else {
            return Optional.empty();
        }
    }

    public List<IMixinConfig> getPendingConfigs() {
        return pendingConfigs.get(mixinProcessor.get(reference));
    }
}
