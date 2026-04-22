package com.ggg.rememo.feature.auth;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintSet;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.base.BaseActivity;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.auth.contract.AuthContract;
import com.ggg.rememo.feature.auth.databinding.ActivityAuthBinding;
import com.ggg.rememo.feature.auth.presenter.AuthPresenter;

import java.util.ArrayList;
import java.util.List;

@Route(path = Routes.Auth.LOGIN)
public class AuthActivity extends BaseActivity<
        ActivityAuthBinding,
        AuthContract.View,
        AuthPresenter>
        implements AuthContract.View {
    private CountDownTimer loginCodeTimer;
    private CountDownTimer regCodeTimer;

    // 动画对象引用，用于在销毁时释放，防止内存泄漏
    private AnimatorSet bgAnimSet;
    private ObjectAnimator compassSpin;
    private AnimatorSet pingAnim;
    private AnimatorSet driftSet;
    private final List<AnimatorSet> particleAnimSets = new ArrayList<>();

    @Override
    protected ActivityAuthBinding inflateBinding(@NonNull LayoutInflater inflater) {
        return ActivityAuthBinding.inflate(inflater);
    }

    @Override
    protected AuthPresenter createPresenter() {
        return new AuthPresenter();
    }

    @Override
    protected AuthContract.View getViewContract() {
        return this;
    }

    @Override
    protected void initView() {
        initAnimations();
        initListeners();
    }

    private void initAnimations() {
        // 背景漫游 (缓慢平移+放大)
        ObjectAnimator bgScaleX = ObjectAnimator.ofFloat(getBinding().ivBgLayer, "scaleX", 1f, 1.05f);
        ObjectAnimator bgScaleY = ObjectAnimator.ofFloat(getBinding().ivBgLayer, "scaleY", 1f, 1.05f);
        ObjectAnimator bgTransX = ObjectAnimator.ofFloat(getBinding().ivBgLayer, "translationX", 0f, -30f);
        ObjectAnimator bgTransY = ObjectAnimator.ofFloat(getBinding().ivBgLayer, "translationY", 0f, -30f);

        bgAnimSet = new AnimatorSet();
        bgAnimSet.playTogether(bgScaleX, bgScaleY, bgTransX, bgTransY);
        bgAnimSet.setDuration(40000);
        bgAnimSet.setInterpolator(new LinearInterpolator());
        bgScaleX.setRepeatCount(ValueAnimator.INFINITE);
        bgScaleX.setRepeatMode(ValueAnimator.REVERSE);
        bgScaleY.setRepeatCount(ValueAnimator.INFINITE);
        bgScaleY.setRepeatMode(ValueAnimator.REVERSE);
        bgTransX.setRepeatCount(ValueAnimator.INFINITE);
        bgTransX.setRepeatMode(ValueAnimator.REVERSE);
        bgTransY.setRepeatCount(ValueAnimator.INFINITE);
        bgTransY.setRepeatMode(ValueAnimator.REVERSE);
        bgAnimSet.start();

        // 罗盘旋转雷达
        compassSpin = ObjectAnimator.ofFloat(getBinding().ivCompassRing, "rotation", 0f, 360f);
        compassSpin.setDuration(120000);
        compassSpin.setRepeatCount(ValueAnimator.INFINITE);
        compassSpin.setInterpolator(new LinearInterpolator());
        compassSpin.start();

        // 坐标标呼吸灯特效
        ObjectAnimator pingScaleX = ObjectAnimator.ofFloat(getBinding().vPingDot, "scaleX", 1f, 3f);
        ObjectAnimator pingScaleY = ObjectAnimator.ofFloat(getBinding().vPingDot, "scaleY", 1f, 3f);
        ObjectAnimator pingAlpha = ObjectAnimator.ofFloat(getBinding().vPingDot, "alpha", 1f, 0f);

        pingAnim = new AnimatorSet();
        pingAnim.playTogether(pingScaleX, pingScaleY, pingAlpha);
        pingAnim.setDuration(2000);
        pingScaleX.setRepeatCount(ValueAnimator.INFINITE);
        pingScaleY.setRepeatCount(ValueAnimator.INFINITE);
        pingAlpha.setRepeatCount(ValueAnimator.INFINITE);
        pingAnim.start();

        // 岁月流沙上升特效
        startParticleAnim(getBinding().vParticle1, 15000, 0);
        startParticleAnim(getBinding().vParticle2, 22000, 4000);

        // 悬浮玻璃卡片抖动
        ObjectAnimator driftY = ObjectAnimator.ofFloat(getBinding().vFloatingCard, "translationY", 0f, -40f);
        ObjectAnimator driftRot = ObjectAnimator.ofFloat(getBinding().vFloatingCard, "rotation", 15f, 25f);
        driftY.setRepeatCount(ValueAnimator.INFINITE);
        driftY.setRepeatMode(ValueAnimator.REVERSE);
        driftRot.setRepeatCount(ValueAnimator.INFINITE);
        driftRot.setRepeatMode(ValueAnimator.REVERSE);
        driftSet = new AnimatorSet();
        driftSet.playTogether(driftY, driftRot);
        driftSet.setDuration(8000);
        driftSet.setInterpolator(new AccelerateDecelerateInterpolator());
        driftSet.start();
    }

    private void startParticleAnim(View particle, long duration, long delay) {
        ObjectAnimator transY = ObjectAnimator.ofFloat(particle, "translationY", 200f, -1500f);
        ObjectAnimator scale = ObjectAnimator.ofFloat(particle, "scaleX", 0.5f, 1.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(particle, "scaleY", 0.5f, 1.5f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(particle, "alpha", 0f, 0.4f, 0f);

        transY.setRepeatCount(ValueAnimator.INFINITE);
        scale.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(transY, scale, scaleY, alpha);
        set.setDuration(duration);
        set.setStartDelay(delay);
        set.start();
        particleAnimSets.add(set);
    }

    private void initListeners() {
        // Tab 切换
        getBinding().tvTabPwd.setOnClickListener(v -> handleTabSwitch(true));
        getBinding().tvTabCode.setOnClickListener(v -> handleTabSwitch(false));

        // 面板切换
        getBinding().tvSwitchRegister.setOnClickListener(v -> switchToRegisterPanel());
        getBinding().tvBackLogin.setOnClickListener(v -> switchToLoginPanel());

        // 登录按钮
        getBinding().btnLogin.setOnClickListener(v -> presenter.login());

        // 注册按钮
        getBinding().btnRegister.setOnClickListener(v -> presenter.register());

        // 获取验证码
        getBinding().tvGetCodeLogin.setOnClickListener(v -> presenter.requestLoginCode());
        getBinding().tvGetCodeReg.setOnClickListener(v -> presenter.requestRegisterCode());
    }

    /**
     * 处理内部 Tab 滑动指示器
     */
    private void handleTabSwitch(boolean isPwdTab) {
        AutoTransition transition = new AutoTransition();
        transition.setDuration(250);
        TransitionManager.beginDelayedTransition(getBinding().flFormContainer, transition);

        // 切换字体与颜色
        getBinding().tvTabPwd.setTextColor(getColor(isPwdTab ? R.color.amber_400 : R.color.white_alpha_40));
        getBinding().tvTabPwd.setTypeface(null, isPwdTab ? Typeface.BOLD : Typeface.NORMAL);

        getBinding().tvTabCode.setTextColor(getColor(!isPwdTab ? R.color.amber_400 : R.color.white_alpha_40));
        getBinding().tvTabCode.setTypeface(null, !isPwdTab ? Typeface.BOLD : Typeface.NORMAL);

        // 指示器平滑移动
        ConstraintSet set = new ConstraintSet();
        set.clone(getBinding().clLoginView);
        if (isPwdTab) {
            set.connect(R.id.v_tab_indicator, ConstraintSet.START, R.id.tv_tab_pwd, ConstraintSet.START);
            set.connect(R.id.v_tab_indicator, ConstraintSet.END, R.id.tv_tab_pwd, ConstraintSet.END);
        } else {
            set.connect(R.id.v_tab_indicator, ConstraintSet.START, R.id.tv_tab_code, ConstraintSet.START);
            set.connect(R.id.v_tab_indicator, ConstraintSet.END, R.id.tv_tab_code, ConstraintSet.END);
        }
        set.applyTo(getBinding().clLoginView);

        // 表单区平滑交替
        getBinding().llPwdFields.setVisibility(isPwdTab ? View.VISIBLE : View.GONE);
        getBinding().llCodeFields.setVisibility(!isPwdTab ? View.VISIBLE : View.GONE);
    }

    // ==================== AuthContract.View 实现 ====================

    @Override
    public void showLoginSuccess(String userId) {
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    @Override
    public void showRegisterSuccess(String userId) {
        Toast.makeText(this, "注册成功，欢迎加入时空拾荒者！", Toast.LENGTH_SHORT).show();
        // 注册成功后自动登录，跳转到主页
        navigateToMain();
    }

    /**
     * 跳转到主页
     */
    private void navigateToMain() {
        ARouter.getInstance()
                .build(Routes.Main.HOME)
                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .navigation();
        finish();
    }

    /**
     * @return 登录手机号
     */
    @Override
    public String getLoginPhone() {
        return getBinding().etLoginAccount.getText().toString();
    }

    /**
     * @return 登录密码
     */
    @Override
    public String getLoginPassword() {
        return getBinding().etLoginPwd.getText().toString();
    }

    /**
     * @return 注册手机号
     */
    @Override
    public String getRegisterPhone() {
        return getBinding().etRegPhone.getText().toString();
    }

    /**
     * @return 注册验证码
     */
    @Override
    public String getRegisterCode() {
        return getBinding().etRegCode.getText().toString();
    }

    /**
     * @return 注册密码
     */
    @Override
    public String getRegisterPassword() {
        return getBinding().etRegPwd.getText().toString();
    }

    /**
     * @return 是否勾选用户协议
     */
    @Override
    public boolean isAgreementChecked() {
        return getBinding().cbAgreement.isChecked();
    }

    private void switchToRegisterPanel() {
        TransitionManager.beginDelayedTransition(getBinding().flFormContainer,
                new ChangeBounds().setDuration(400));

        getBinding().clRegisterView.setVisibility(View.VISIBLE);
        getBinding().clRegisterView.setTranslationX(150f);
        getBinding().clRegisterView.setAlpha(0f);

        getBinding().clLoginView.animate()
                .alpha(0f)
                .translationX(-150f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> getBinding().clLoginView.setVisibility(View.GONE))
                .start();

        getBinding().clRegisterView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void switchToLoginPanel() {
        TransitionManager.beginDelayedTransition(getBinding().flFormContainer,
                new ChangeBounds().setDuration(400));

        getBinding().clLoginView.setVisibility(View.VISIBLE);
        getBinding().clLoginView.setTranslationX(-150f);
        getBinding().clLoginView.setAlpha(0f);

        getBinding().clRegisterView.animate()
                .alpha(0f)
                .translationX(150f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> getBinding().clRegisterView.setVisibility(View.GONE))
                .start();

        getBinding().clLoginView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginCountDown() {
        startCountdown(getBinding().tvGetCodeLogin, true);
    }

    @Override
    public void showRegisterCountDown() {
        startCountdown(getBinding().tvGetCodeReg, false);
    }


    // 验证码倒计时
    public void startCountdown(TextView targetButton, boolean isLogin) {
        targetButton.setEnabled(false);
        targetButton.setTextColor(getColor(R.color.amber_500));

        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                targetButton.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                targetButton.setEnabled(true);
                targetButton.setBackgroundResource(R.drawable.shape_btn_code);
                targetButton.setTextColor(getColor(R.color.white_alpha_80));
                targetButton.setText("获取验证码");
            }
        };

        if (isLogin) {
            if (loginCodeTimer != null) loginCodeTimer.cancel();
            loginCodeTimer = timer;
        } else {
            if (regCodeTimer != null) regCodeTimer.cancel();
            regCodeTimer = timer;
        }
        timer.start();
    }

    @Override
    protected void onDestroy() {
        // 取消计时器
        if (loginCodeTimer != null) loginCodeTimer.cancel();
        if (regCodeTimer != null) regCodeTimer.cancel();

        // 取消所有无限循环动画，防止内存泄漏
        if (bgAnimSet != null) bgAnimSet.cancel();
        if (compassSpin != null) compassSpin.cancel();
        if (pingAnim != null) pingAnim.cancel();
        if (driftSet != null) driftSet.cancel();
        for (AnimatorSet set : particleAnimSets) {
            if (set != null) set.cancel();
        }
        particleAnimSets.clear();

        super.onDestroy();
    }
}
