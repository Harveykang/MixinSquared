package com.bawnorton.mixinsquared.member_canceller;

import com.bawnorton.mixinsquared.api.MixinMemberCanceller;

@SuppressWarnings("unused")
public class MixinMemberCancellerRegistrar {
    /**
     * Registers a MixinMemberCanceller to be used by the ExtensionCancelMixinMember.
     * @param canceller The MixinMemberCanceller to register.
     */
    public static void register(MixinMemberCanceller canceller) {
        ExtensionCancelMemberApplication.CANCELLERS.add(canceller);
        ExtensionCancelMemberApplication.LOGGER.debug("Registered mixin member canceller {}", canceller.getClass().getName());
    }
}
