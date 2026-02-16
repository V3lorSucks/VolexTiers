package com.volextierlist.tiertagger.version.compat;

import com.volextierlist.tiertagger.version.VersionSupport;

public final class CompatBridgeFactory {
    private static final ClientCompatBridge BRIDGE = create();

    private CompatBridgeFactory() {}

    public static ClientCompatBridge client() {
        return BRIDGE;
    }

    private static ClientCompatBridge create() {
        VersionSupport.requireSupportedOrThrow();
        return new ClientCompatBridge121();
    }
}
