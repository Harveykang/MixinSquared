package com.bawnorton.mixinsquared.node_canceller;

import org.spongepowered.asm.mixin.throwables.MixinException;

public class InvalidMixinMemberCancellerException extends MixinException {
    public InvalidMixinMemberCancellerException(String message) {
        super(message);
    }
}
