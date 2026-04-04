package com.ggg.rememo.feature.auth.presenter;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.model.network.response.AuthResponse;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.core.data.service.UserService;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.NetworkClient;
import com.ggg.rememo.feature.auth.contract.AuthContract;
import com.ggg.rememo.feature.auth.data.AuthRepository;

import java.util.regex.Pattern;

/**
 * 登录注册模块 Presenter。
 * 职责：参数校验、调用 Repository、处理登录注册结果。
 */
public class AuthPresenter extends BasePresenter<AuthContract.View> implements AuthContract.Presenter {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,20}$");

    private final AuthRepository repository;
    private UserService userService;
    private final Handler mainHandler;

    public AuthPresenter() {
        this.repository = new AuthRepository();
        this.mainHandler = new Handler(Looper.getMainLooper());
        // 通过 ARouter 获取 UserService，避免直接依赖 profile 模块
        this.userService = (UserService) ARouter.getInstance()
                .build(Routes.Profile.USER_SERVICE)
                .navigation();
    }

    // ========== 登录 ==========

    @Override
    public void login() {
        ifViewAttached(view -> {
            String phone = view.getLoginPhone();
            String password = view.getLoginPassword();

            // 客户端校验
            if (!validatePhone(phone, view)) return;
            if (!validatePassword(password, view)) return;


            repository.login(phone, password, new ApiCallback<AuthResponse>() {
                @Override
                public void onSuccess(AuthResponse data) {
                    mainHandler.post(() -> {
                        saveAndNotifyLoginSuccess(view, data);
                    });
                }

                @Override
                public void onError(String message) {
                    mainHandler.post(() -> {
                        view.showError(message);
                    });
                }
            });
        });
    }

    // ========== 注册 ==========
    @Override
    public void register() {
        ifViewAttached(view -> {
            String phone = view.getRegisterPhone();
            String code = view.getRegisterCode();
            String password = view.getRegisterPassword();
            boolean agreed = view.isAgreementChecked();

            // 客户端校验
            if (!validatePhone(phone, view)) return;
            if (!validatePassword(password, view)) return;
            if (!agreed) {
                view.showError("请先同意《拾忆条款》与《隐私政策》");
                return;
            }

            repository.register(phone, password, new ApiCallback<AuthResponse>() {
                @Override
                public void onSuccess(AuthResponse data) {
                    mainHandler.post(() -> {
                        saveAndNotifyRegisterSuccess(view, data);
                    });
                }

                @Override
                public void onError(String message) {
                    mainHandler.post(() -> {
                        view.showError(message);
                    });
                }
            });
        });
    }
    // ========== 验证码 ==========
    @Override
    public void requestLoginCode() {
        ifViewAttached(view -> {
            String phone = view.getLoginPhone();
            if (!validatePhone(phone, view)) return;
            view.showLoginCountDown();

            repository.sendLoginCode(phone, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    mainHandler.post(() -> view.showError("验证码已发送"));
                }

                @Override
                public void onError(String message) {
                    mainHandler.post(() -> view.showError(message));
                }
            });
        });
    }

    @Override
    public void requestRegisterCode() {
        ifViewAttached(view -> {
            String phone = view.getRegisterPhone();
            if (!validatePhone(phone, view)) return;
            view.showRegisterCountDown();

            repository.sendRegisterCode(phone, new ApiCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    mainHandler.post(() -> view.showError("验证码已发送"));
                }

                @Override
                public void onError(String message) {
                    mainHandler.post(() -> view.showError(message));
                }
            });
        });
    }

    private boolean validatePhone(String phone, AuthContract.View view) {
        if (phone == null || phone.trim().isEmpty()) {
            view.showError("手机号不能为空");
            return false;
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            view.showError("手机号格式不正确");
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password, AuthContract.View view) {
        if (password == null || password.isEmpty()) {
            view.showError("密码不能为空");
            return false;
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            view.showError("密码长度需在6-20位之间");
            return false;
        }
        return true;
    }

    private void saveAndNotifyLoginSuccess(AuthContract.View view, AuthResponse data) {
        TokenManager.saveToken(data.getToken(), data.getExpiresAt(), data.getUserId());
        NetworkClient.setAuthToken(data.getToken());

        // 登录成功后立即获取并保存用户信息到本地数据库
        userService.getUserInfo(new UserService.UserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                userService.saveUserInfo(userInfo, new UserService.Callback() {
                    @Override
                    public void onSuccess() {
                        mainHandler.post(() -> view.showLoginSuccess(data.getUserId()));
                    }

                    @Override
                    public void onError(String message) {
                        mainHandler.post(() -> view.showLoginSuccess(data.getUserId()));
                    }
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> view.showLoginSuccess(data.getUserId()));
            }
        });
    }

    private void saveAndNotifyRegisterSuccess(AuthContract.View view, AuthResponse data) {
        TokenManager.saveToken(data.getToken(), data.getExpiresAt(), data.getUserId());
        NetworkClient.setAuthToken(data.getToken());

        // 注册成功后立即获取并保存用户信息到本地数据库
        userService.getUserInfo(new UserService.UserInfoCallback() {
            @Override
            public void onSuccess(UserInfo userInfo) {
                userService.saveUserInfo(userInfo, new UserService.Callback() {
                    @Override
                    public void onSuccess() {
                        mainHandler.post(() -> view.showRegisterSuccess(data.getUserId()));
                    }

                    @Override
                    public void onError(String message) {
                        mainHandler.post(() -> view.showRegisterSuccess(data.getUserId()));
                    }
                });
            }

            @Override
            public void onError(String message) {
                mainHandler.post(() -> view.showRegisterSuccess(data.getUserId()));
            }
        });
    }
}
