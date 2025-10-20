package com.orange.userservice.activity.web;

public final class RequestMetadataHolder {
    private static final ThreadLocal<RequestMetadata> TL = new ThreadLocal<>();
    private RequestMetadataHolder() {}
    public static void set(RequestMetadata md) { TL.set(md); }
    public static RequestMetadata get() { return TL.get(); }
    public static void clear() { TL.remove(); }
}
