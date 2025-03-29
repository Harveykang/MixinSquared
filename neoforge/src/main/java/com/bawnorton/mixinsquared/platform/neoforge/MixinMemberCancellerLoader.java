package com.bawnorton.mixinsquared.platform.neoforge;

import com.bawnorton.mixinsquared.api.MixinMemberCanceller;
import com.bawnorton.mixinsquared.member_canceller.MixinMemberCancellerRegistrar;

import java.util.ServiceLoader;

public final class MixinMemberCancellerLoader {
    private static final ServiceLoader<MixinMemberCanceller> ENTRYPOINTS = ServiceLoader.load(MixinMemberCanceller.class);

    public static void load() {
        ENTRYPOINTS.forEach(MixinMemberCancellerRegistrar::register);
    }
}
