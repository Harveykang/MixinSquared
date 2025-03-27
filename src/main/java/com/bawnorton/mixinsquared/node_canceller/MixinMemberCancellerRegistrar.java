package com.bawnorton.mixinsquared.node_canceller;

import com.bawnorton.mixinsquared.api.MixinMemberCanceller;

@SuppressWarnings("unused")
public class MixinMemberCancellerRegistrar {
    /**
     * Registers a MixinMemberCanceller to be used by the ExtensionCancelMixinMember.
     * @param canceller The MixinMemberCanceller to register.
     */
    public static void register(MixinMemberCanceller canceller) {
        ExtensionCancelMixinMember.CANCELLERS.add(canceller);
        ExtensionCancelMixinMember.LOGGER.debug("Registered mixin member canceller {}", canceller.getClass().getName());
    }
}
