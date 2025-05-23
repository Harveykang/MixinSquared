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

package com.bawnorton.mixinsquared.adjuster.tools;

import com.bawnorton.mixinsquared.adjuster.tools.type.ConstraintAnnotationNode;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.AnnotationNode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class AdjustableOverwriteNode extends RemapperHolderAnnotationNode implements ConstraintAnnotationNode {
    public AdjustableOverwriteNode(AnnotationNode node) {
        super(node);
    }

    public static AdjustableOverwriteNode defaultNode() {
        AnnotationNode node = new AnnotationNode(KnownAnnotations.OVERWRITE.desc());
        return new AdjustableOverwriteNode(node);
    }

    @Override
    public AdjustableOverwriteNode withConstraints(UnaryOperator<String> constraints) {
        return (AdjustableOverwriteNode) ConstraintAnnotationNode.super.withConstraints(constraints);
    }

    public List<String> getAliases() {
        return this.<List<String>>get("aliases").orElse(new ArrayList<>());
    }

    public void setAliases(List<String> aliases) {
        this.set("aliases", aliases);
    }

    public AdjustableOverwriteNode withAliases(UnaryOperator<List<String>> aliases) {
        this.setAliases(aliases.apply(this.getAliases()));
        return this;
    }

    @Override
    @ApiStatus.Internal
    public void applyRefmap(UnaryOperator<String> refmapApplicator) {
        // no-op - Overwrite does not use the refmap currently
    }
}
