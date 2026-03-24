package org.sainm.model;

public final class OcrOptions {
    public enum ProviderType { LOCAL_PADDLE, REMOTE, AUTO }
    public enum CacheScope { PAGE, FILE }

    private ProviderType providerType = ProviderType.AUTO;
    private String language = "zh";
    private String remoteEndpoint;
    private String remoteApiKey;
    private String modelDir;
    private boolean enableCache = true;
    private String cacheDir = System.getProperty("user.home") + "/.pdf-compare/ocr-cache";
    private CacheScope cacheScope = CacheScope.PAGE;
    private double minConfidence = 0.6;

    public static OcrOptions defaults() { return new OcrOptions(); }

    public ProviderType getProviderType() { return providerType; }
    public void setProviderType(ProviderType providerType) { this.providerType = providerType; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getRemoteEndpoint() { return remoteEndpoint; }
    public void setRemoteEndpoint(String remoteEndpoint) { this.remoteEndpoint = remoteEndpoint; }
    public String getRemoteApiKey() { return remoteApiKey; }
    public void setRemoteApiKey(String remoteApiKey) { this.remoteApiKey = remoteApiKey; }
    public String getModelDir() { return modelDir; }
    public void setModelDir(String modelDir) { this.modelDir = modelDir; }
    public boolean isEnableCache() { return enableCache; }
    public void setEnableCache(boolean enableCache) { this.enableCache = enableCache; }
    public String getCacheDir() { return cacheDir; }
    public void setCacheDir(String cacheDir) { this.cacheDir = cacheDir; }
    public CacheScope getCacheScope() { return cacheScope; }
    public void setCacheScope(CacheScope cacheScope) { this.cacheScope = cacheScope; }
    public double getMinConfidence() { return minConfidence; }
    public void setMinConfidence(double minConfidence) { this.minConfidence = minConfidence; }
}
