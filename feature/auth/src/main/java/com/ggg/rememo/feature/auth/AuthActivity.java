package com.ggg.rememo.feature.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.auth.contract.AuthContract;
import com.ggg.rememo.feature.auth.databinding.ActivityAuthBinding;
import com.ggg.rememo.feature.auth.presenter.AuthPresenter;


@Route(path = Routes.Auth.LOGIN)
public class AuthActivity extends AppCompatActivity implements AuthContract.View {

    private ActivityAuthBinding binding;
    private AuthPresenter presenter;
    private CountDownTimer loginCodeTimer;
    private CountDownTimer regCodeTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        presenter = new AuthPresenter();
        presenter.attachView(this);

        initAnimations();
        initListeners();
    }

    private void initAnimations() {
        // 背景漫游 (缓慢平移+放大)
        ObjectAnimator bgScaleX = ObjectAnimator.ofFloat(binding.ivBgLayer, "scaleX", 1f, 1.05f);
        ObjectAnimator bgScaleY = ObjectAnimator.ofFloat(binding.ivBgLayer, "scaleY", 1f, 1.05f);
        ObjectAnimator bgTransX = ObjectAnimator.ofFloat(binding.ivBgLayer, "translationX", 0f, -30f);
        ObjectAnimator bgTransY = ObjectAnimator.ofFloat(binding.ivBgLayer, "translationY", 0f, -30f);

        AnimatorSet bgAnimSet = new AnimatorSet();
        bgAnimSet.playTogether(bgScaleX, bgScaleY, bgTransX, bgTransY);
        bgAnimSet.setDuration(40000);
        bgAnimSet.setInterpolator(new LinearInterpolator());
        bgScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        bgScaleX.setRepeatMode(ObjectAnimator.REVERSE);
        bgScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        bgScaleY.setRepeatMode(ObjectAnimator.REVERSE);
        bgTransX.setRepeatCount(ObjectAnimator.INFINITE);
        bgTransX.setRepeatMode(ObjectAnimator.REVERSE);
        bgTransY.setRepeatCount(ObjectAnimator.INFINITE);
        bgTransY.setRepeatMode(ObjectAnimator.REVERSE);
        bgAnimSet.start();

        // 罗盘旋转雷达
        ObjectAnimator compassSpin = ObjectAnimator.ofFloat(binding.ivCompassRing, "rotation", 0f, 360f);
        compassSpin.setDuration(120000);
        compassSpin.setRepeatCount(ObjectAnimator.INFINITE);
        compassSpin.setInterpolator(new LinearInterpolator());
        compassSpin.start();

        // 坐标标呼吸灯特效
        ObjectAnimator pingScaleX = ObjectAnimator.ofFloat(binding.vPingDot, "scaleX", 1f, 3f);
        ObjectAnimator pingScaleY = ObjectAnimator.ofFloat(binding.vPingDot, "scaleY", 1f, 3f);
        ObjectAnimator pingAlpha = ObjectAnimator.ofFloat(binding.vPingDot, "alpha", 1f, 0f);

        AnimatorSet pingAnim = new AnimatorSet();
        pingAnim.playTogether(pingScaleX, pingScaleY, pingAlpha);
        pingAnim.setDuration(2000);
        pingScaleX.setRepeatCount(ObjectAnimator.INFINITE);
        pingScaleY.setRepeatCount(ObjectAnimator.INFINITE);
        pingAlpha.setRepeatCount(ObjectAnimator.INFINITE);
        pingAnim.start();

        // 岁月流沙上升特效
        startParticleAnim(binding.vParticle1, 15000, 0);
        startParticleAnim(binding.vParticle2, 22000, 4000);

        // 悬浮玻璃卡片抖动
        ObjectAnimator driftY = ObjectAnimator.ofFloat(binding.vFloatingCard, "translationY", 0f, -40f);
        ObjectAnimator driftRot = ObjectAnimator.ofFloat(binding.vFloatingCard, "rotation", 15f, 25f);
        driftY.setRepeatCount(ObjectAnimator.INFINITE);
        driftY.setRepeatMode(ObjectAnimator.REVERSE);
        driftRot.setRepeatCount(ObjectAnimator.INFINITE);
        driftRot.setRepeatMode(ObjectAnimator.REVERSE);
        AnimatorSet driftSet = new AnimatorSet();
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

        transY.setRepeatCount(ObjectAnimator.INFINITE);
        scale.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(transY, scale, scaleY, alpha);
        set.setDuration(duration);
        set.setStartDelay(delay);
        set.start();
    }

    private void initListeners() {
        // Tab 切换
        binding.tvTabPwd.setOnClickListener(v -> handleTabSwitch(true));
        binding.tvTabCode.setOnClickListener(v -> handleTabSwitch(false));

        // 面板切换
        binding.tvSwitchRegister.setOnClickListener(v -> switchToRegisterPanel());
        binding.tvBackLogin.setOnClickListener(v -> switchToLoginPanel());

        // 登录按钮
        binding.btnLogin.setOnClickListener(v -> presenter.login());

        // 注册按钮
        binding.btnRegister.setOnClickListener(v -> presenter.register());

        // 获取验证码
        binding.tvGetCodeLogin.setOnClickListener(v -> presenter.requestLoginCode());
        binding.tvGetCodeReg.setOnClickListener(v -> presenter.requestRegisterCode());
    }

    /**
     * 处理内部 Tab 滑动指示器
     */
    private void handleTabSwitch(boolean isPwdTab) {
        AutoTransition transition = new AutoTransition();
        transition.setDuration(250);
        TransitionManager.beginDelayedTransition(binding.flFormContainer, transition);

        // 切换字体与颜色
        binding.tvTabPwd.setTextColor(getColor(isPwdTab ? R.color.amber_400 : R.color.white_alpha_40));
        binding.tvTabPwd.setTypeface(null, isPwdTab ? Typeface.BOLD : Typeface.NORMAL);

        binding.tvTabCode.setTextColor(getColor(!isPwdTab ? R.color.amber_400 : R.color.white_alpha_40));
        binding.tvTabCode.setTypeface(null, !isPwdTab ? Typeface.BOLD : Typeface.NORMAL);

        // 指示器平滑移动
        ConstraintSet set = new ConstraintSet();
        set.clone(binding.clLoginView);
        if (isPwdTab) {
            set.connect(R.id.v_tab_indicator, ConstraintSet.START, R.id.tv_tab_pwd, ConstraintSet.START);
            set.connect(R.id.v_tab_indicator, ConstraintSet.END, R.id.tv_tab_pwd, ConstraintSet.END);
        } else {
            set.connect(R.id.v_tab_indicator, ConstraintSet.START, R.id.tv_tab_code, ConstraintSet.START);
            set.connect(R.id.v_tab_indicator, ConstraintSet.END, R.id.tv_tab_code, ConstraintSet.END);
        }
        set.applyTo(binding.clLoginView);

        // 表单区平滑交替
        binding.llPwdFields.setVisibility(isPwdTab ? View.VISIBLE : View.GONE);
        binding.llCodeFields.setVisibility(!isPwdTab ? View.VISIBLE : View.GONE);
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
        return binding == null ? null : binding.etLoginAccount.getText().toString();
    }

    /**
     * @return 登录密码
     */
    @Override
    public String getLoginPassword() {
        return binding == null ? null : binding.etLoginPwd.getText().toString();
    }

    /**
     * @return 注册手机号
     */
    @Override
    public String getRegisterPhone() {
        return binding == null ? null : binding.etRegPhone.getText().toString();
    }

    /**
     * @return 注册验证码
     */
    @Override
    public String getRegisterCode() {
        return binding == null ? null : binding.etRegCode.getText().toString();
    }

    /**
     * @return 注册密码
     */
    @Override
    public String getRegisterPassword() {
        return binding == null ? null : binding.etRegPwd.getText().toString();
    }

    /**
     * @return 是否勾选用户协议
     */
    @Override
    public boolean isAgreementChecked() {
        return binding != null && binding.cbAgreement.isChecked();
    }

    private void switchToRegisterPanel() {
        TransitionManager.beginDelayedTransition(binding.flFormContainer,
                new ChangeBounds().setDuration(400));

        binding.clRegisterView.setVisibility(View.VISIBLE);
        binding.clRegisterView.setTranslationX(150f);
        binding.clRegisterView.setAlpha(0f);

        binding.clLoginView.animate()
                .alpha(0f)
                .translationX(-150f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> binding.clLoginView.setVisibility(View.GONE))
                .start();

        binding.clRegisterView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void switchToLoginPanel() {
        TransitionManager.beginDelayedTransition(binding.flFormContainer,
                new ChangeBounds().setDuration(400));

        binding.clLoginView.setVisibility(View.VISIBLE);
        binding.clLoginView.setTranslationX(-150f);
        binding.clLoginView.setAlpha(0f);

        binding.clRegisterView.animate()
                .alpha(0f)
                .translationX(150f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> binding.clRegisterView.setVisibility(View.GONE))
                .start();

        binding.clLoginView.animate()
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
        startCountdown(binding.tvGetCodeLogin, true);
    }

    @Override
    public void showRegisterCountDown() {
        startCountdown(binding.tvGetCodeReg, false);
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



    // ==================== 生命周期 ====================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
        if (loginCodeTimer != null) {
            loginCodeTimer.cancel();
        }
        if (regCodeTimer != null) {
            regCodeTimer.cancel();
        }
        binding = null;
    }
}
