.class public Lcom/example/verifyapp/MainActivity;
.super Landroid/app/Activity;
.source "MainActivity.java"


# instance fields
.field private verificationCode:Ljava/lang/String;


# direct methods
.method public constructor <init>()V
    .locals 1

    .prologue
    .line 8
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    .line 10
    const-string v0, "VALID123"

    iput-object v0, p0, Lcom/example/verifyapp/MainActivity;->verificationCode:Ljava/lang/String;

    return-void
.end method


# virtual methods
.method public verifyInput(Ljava/lang/String;)Z
    .locals 2
    .param p1, "input"    # Ljava/lang/String;

    .prologue
    .line 20
    # MODIFIED: Always return true (1) to bypass verification
    # Original code checked: iget-object v0, p0, Lcom/example/verifyapp/MainActivity;->verificationCode:Ljava/lang/String;
    # Original code checked: invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    
    .line 21
    const/4 v1, 0x1

    return v1
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 3
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .prologue
    .line 30
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 32
    new-instance v0, Landroid/widget/TextView;

    invoke-direct {v0, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 33
    .local v0, "textView":Landroid/widget/TextView;
    const-string v1, "Enter verification code"

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 34
    invoke-virtual {p0, v0}, Landroid/app/Activity;->setContentView(Landroid/view/View;)V

    .line 36
    # MODIFIED: Removed trial check - commented out the following lines:
    # invoke-direct {p0}, Lcom/example/verifyapp/MainActivity;->checkTrial()Z
    # move-result v1
    # if-eqz v1, :cond_0
    # const-string v1, "Trial version - Limited features"
    # const/4 v2, 0x1
    # invoke-static {p0, v1, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
    # move-result-object v1
    # invoke-virtual {v1}, Landroid/widget/Toast;->show()V
    # :cond_0
    
    .line 39
    return-void
.end method

.method private checkTrial()Z
    .locals 1

    .prologue
    .line 45
    # MODIFIED: Always return false (0) to disable trial mode
    # Original code: const/4 v0, 0x1
    const/4 v0, 0x0

    return v0
.end method
