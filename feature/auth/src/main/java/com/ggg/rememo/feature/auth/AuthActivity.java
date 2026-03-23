package com.ggg.rememo.feature.auth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.ggg.rememo.feature.auth.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;

    private CountDownTimer loginCodeTimer;
    private CountDownTimer regCodeTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        // 循环往复
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
        binding.tvSwitchRegister.setOnClickListener(v -> slideToRegister());
        binding.tvBackLogin.setOnClickListener(v -> slideToLogin());

        // 绑定获取验证码的点击事件
        binding.tvGetCodeLogin.setOnClickListener(v -> startCountdown(binding.tvGetCodeLogin, true));
        binding.tvGetCodeReg.setOnClickListener(v -> startCountdown(binding.tvGetCodeReg, false));

    }

    /**
     * 处理内部 Tab 滑动指示器
     */
    private void handleTabSwitch(boolean isPwdTab) {
        AutoTransition transition = new AutoTransition();
        transition.setDuration(250);
        TransitionManager.beginDelayedTransition(binding.flFormContainer, transition);

        // 切换字体与颜色
        binding.tvTabPwd.setTextColor(ContextCompat.getColor(this, isPwdTab ? R.color.amber_400 : R.color.white_alpha_40));
        binding.tvTabPwd.setTypeface(null, isPwdTab ? Typeface.BOLD : Typeface.NORMAL);

        binding.tvTabCode.setTextColor(ContextCompat.getColor(this, !isPwdTab ? R.color.amber_400 : R.color.white_alpha_40));
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

    /**
     * 出入场动画 (向左移出，注册框进入)
     */
    private void slideToRegister() {
        TransitionManager.beginDelayedTransition(binding.flFormContainer, new ChangeBounds().setDuration(400));

        binding.clRegisterView.setVisibility(View.VISIBLE);
        binding.clRegisterView.setTranslationX(150f);

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



    /**
     * 出入场动画 (向右移出，登录框进入)
     */
    private void slideToLogin() {
        TransitionManager.beginDelayedTransition(binding.flFormContainer, new ChangeBounds().setDuration(400));

        binding.clLoginView.setVisibility(View.VISIBLE);
        binding.clLoginView.setTranslationX(-150f);

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

    private void startCountdown(TextView targetButton, boolean isLogin) {
        targetButton.setEnabled(false);
        targetButton.setTextColor(ContextCompat.getColor(this, R.color.amber_500));

        // 初始化 CountDownTimer，总时长 60000 毫秒(60秒)，间隔 1000 毫秒(1秒)
        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 每次倒计时触发，更新文字
                int seconds = (int) (millisUntilFinished / 1000);
                targetButton.setText(seconds + "s");
            }

            @Override
            public void onFinish() {
                // 倒计时结束，恢复原样
                targetButton.setEnabled(true);
                targetButton.setBackgroundResource(R.drawable.shape_btn_code);
                targetButton.setTextColor(ContextCompat.getColor(AuthActivity.this, R.color.white_alpha_80));
                targetButton.setText("获取验证码");
            }
        };

        // 赋值给全局变量并启动
        if (isLogin) {
            loginCodeTimer = timer;
        } else {
            regCodeTimer = timer;
        }
        timer.start();
    }

    // ================= 极其重要的一步 =================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 页面销毁时必须取消定时器，否则后台会一直跑，引发 NullPointerException 或内存泄漏
        if (loginCodeTimer != null) {
            loginCodeTimer.cancel();
        }
        if (regCodeTimer != null) {
            regCodeTimer.cancel();
        }
        binding = null;
    }
}
