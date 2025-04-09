/*
 * MIT License
 *
 * Copyright (c) 2023-present Bawnorton
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bawnorton.mixinsquared.platform.fabric;

import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import com.bawnorton.mixinsquared.api.MixinCanceller;
import com.bawnorton.mixinsquared.api.MixinTargetModifier;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import com.bawnorton.mixinsquared.target_modifier.MixinTargetsModifierRegistrar;
import net.fabricmc.loader.api.FabricLoader;

public final class MixinSquaredApiImplLoader {
    public static void load() {
        FabricLoader.getInstance().getEntrypointContainers("mixinsquared", MixinCanceller.class).forEach(container -> {
            try {
                MixinCanceller canceller = container.getEntrypoint();
                MixinCancellerRegistrar.register(canceller);
            } catch (Throwable e) {
                String id = container.getProvider().getMetadata().getId();
                System.err.printf("Mod %s provides a broken MixinCanceller implementation:\n", id);
                e.printStackTrace(System.err);
            }
        });
        FabricLoader.getInstance().getEntrypointContainers("mixinsquared-adjuster", MixinAnnotationAdjuster.class).forEach(container -> {
            try {
                MixinAnnotationAdjuster annotationAdjuster = container.getEntrypoint();
                MixinAnnotationAdjusterRegistrar.register(annotationAdjuster);
            } catch (Throwable e) {
                String id = container.getProvider().getMetadata().getId();
                System.err.printf("Mod %s provides a broken MixinAnnotationAdjuster implementation:\n", id);
                e.printStackTrace(System.err);
            }
        });
        FabricLoader.getInstance().getEntrypointContainers("mixinsquared-target-modifier", MixinTargetModifier.class).forEach(container -> {
            try {
                MixinTargetModifier mixinTargetModifier = container.getEntrypoint();
                MixinTargetsModifierRegistrar.register(mixinTargetModifier);
            } catch (Throwable e) {
                String id = container.getProvider().getMetadata().getId();
                System.err.printf("Mod %s provides a broken MixinTargetModifier implementation:\n", id);
                e.printStackTrace(System.err);
            }
        });
    }
}
