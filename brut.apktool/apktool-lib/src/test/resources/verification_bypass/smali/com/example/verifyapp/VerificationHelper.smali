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
    const-string v0, "ABC123XYZ789"

    .line 11
    .local v0, "validKey":Ljava/lang/String;
    invoke-virtual {v0, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    return v1
.end method

.method public static checkExpiration()Z
    .locals 4

    .prologue
    .line 20
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v0

    .line 21
    .local v0, "currentTime":J
    const-wide v2, 0x18ba8f00400L    # Trial expiration timestamp

    .line 22
    cmp-long v2, v0, v2

    if-gez v2, :cond_0

    .line 23
    const/4 v2, 0x1

    return v2

    .line 25
    :cond_0
    const/4 v2, 0x0

    return v2
.end method
