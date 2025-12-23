.class public Lcom/example/verifyapp/VerificationHelper;
.super Ljava/lang/Object;
.source "VerificationHelper.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .prologue
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# static methods
.method public static isValidLicense(Ljava/lang/String;)Z
    .locals 2
    .param p0, "licenseKey"    # Ljava/lang/String;

    .prologue
    .line 10
    # MODIFIED: Always return true - any license key is valid
    # Original: const-string v0, "ABC123XYZ789"
    # Original: invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    # Original: move-result v1
    
    .line 11
    const/4 v1, 0x1

    return v1
.end method

.method public static checkExpiration()Z
    .locals 4

    .prologue
    .line 20
    # MODIFIED: Always return false - never expired
    # Original code checked current time against expiration timestamp
    # Original: invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    # Original: move-result-wide v0
    # Original: const-wide v2, 0x18ba8f00400L
    # Original: cmp-long v2, v0, v2
    # Original: if-gez v2, :cond_0
    # Original: const/4 v2, 0x1
    # Original: return v2
    # Original: :cond_0
    
    .line 21
    const/4 v2, 0x0

    return v2
.end method
