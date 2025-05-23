/*
 * MIT License
 *
 * Copyright (c) 2025-present Bawnorton
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

package com.bawnorton.mixinsquared.adjuster.tools.type;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAtNode;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.AnnotationNode;
import java.util.function.UnaryOperator;

public interface AtAnnotationNode extends RemappableAnnotationNode {
    default AdjustableAtNode getAt() {
        return this.<AnnotationNode>get("at")
                   .map(node -> {
                       AdjustableAtNode at = new AdjustableAtNode(node);
                       at.setRemapper(this.getRemapper());
                       return at;
                   })
                   .orElse(null);
    }

    default void setAt(AdjustableAtNode at) {
        this.set("at", at);
    }

    default AtAnnotationNode withAt(UnaryOperator<AdjustableAtNode> at) {
        this.setAt(at.apply(this.getAt()));
        return this;
    }

    @Override
    @ApiStatus.Internal
    default void applyRefmap(UnaryOperator<String> refmapApplicator) {
        this.withAt(at -> {
            at.applyRefmap(refmapApplicator);
            return at;
        });
    }
}
